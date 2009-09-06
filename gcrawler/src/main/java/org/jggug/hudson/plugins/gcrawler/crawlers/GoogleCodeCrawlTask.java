package org.jggug.hudson.plugins.gcrawler.crawlers;

import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;
import static org.jggug.hudson.plugins.gcrawler.util.HttpUtils.existsFile;
import static org.jggug.hudson.plugins.gcrawler.util.HttpUtils.getFile;
import static org.jggug.hudson.plugins.gcrawler.util.HttpUtils.joinAsPath;
import static org.jggug.hudson.plugins.gcrawler.util.JobTemplate.createTemplate;
import static org.jggug.hudson.plugins.gcrawler.util.PropertyFileUtils.getStringPropertyValue;
import static org.jggug.hudson.plugins.gcrawler.util.PropertyFileUtils.toResourceBundleFromText;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.plugins.googlecode.GoogleCodeProjectProperty;
import hudson.plugins.googlecode.GoogleCodeRepositoryBrowser;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;
import hudson.tasks.Builder;
import hudson.tasks.LogRotator;
import hudson.tasks.Publisher;
import hudson.tasks.Shell;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.triggers.SCMTrigger;
import hudson.util.DescribableList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.PropertyResourceBundle;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jggug.hudson.plugins.gcrawler.CrawlContext;
import org.jggug.hudson.plugins.gcrawler.GBuildWrapper;
import org.jggug.hudson.plugins.gcrawler.GrailsProjectInfo;
import org.jggug.hudson.plugins.gcrawler.SVNFIleInfo;
import org.jggug.hudson.plugins.gcrawler.SVNRepositoryWrapper;
import org.jggug.hudson.plugins.gcrawler.util.JobTemplate;

import com.g2one.hudson.grails.GrailsBuilder;

class GoogleCodeCrawlTask implements Callable<GrailsProjectInfo> {

    private static final String VERSION = "app.version";

    private static final String SERVLET_VERSION = "app.servlet.version";

    private static final String GRAILS_VERSION = "app.grails.version";

    private static final String NAME = "app.name";

    private static final Pattern PLUGINS = compile("plugins\\.(.*?)=(.*)");

    private static final Pattern LICENSE = compile("<a .*? rel=\"nofollow\">.*?</a>");

    private static final JobTemplate JOB_DESCRIPTION = createTemplate("google_grails_description.txt");

    private static final JobTemplate JOB_SHELL = createTemplate("google_grails_shell.txt");

    private static final String LISENCE_NA = "N/A";

    private static final Field F_BUILD_WRAPPERS;

    static {
        try {
            F_BUILD_WRAPPERS = Project.class.getDeclaredField("buildWrappers");
            F_BUILD_WRAPPERS.setAccessible(true);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private String name;

    private CrawlContext context;

    public GoogleCodeCrawlTask(String name, CrawlContext context) {
        this.name = name;
        this.context = context;
    }

    public GrailsProjectInfo call() throws Exception {
        GrailsProjectInfo result = createProjectInfo();
        if (!result.hasError()) {
            saveAsHudsonJob(result);
        }
        context.getLogger().info(format("%s grailsVersion:%s %s(ms)",
            result.getName(), result.getGrailsVersion(), result.getParseTime()));
        return result;
    }

    protected void setupAppProperties(GrailsProjectInfo info, String text) {
        try {
            PropertyResourceBundle b = toResourceBundleFromText(text);
            info.setAppName(getStringPropertyValue(b, NAME));
            info.setVersion(getStringPropertyValue(b, VERSION));
            info.setGrailsVersion(getStringPropertyValue(b, GRAILS_VERSION));
            info.setServletVersion(getStringPropertyValue(b, SERVLET_VERSION));
            Matcher m = PLUGINS.matcher(text);
            while (m.find()) {
                info.addPlugin(format("%s-%s", m.group(1), m.group(2)));
            }
        } catch (IOException e) {
            info.setErrorMessage("invalid application.properties.");
        }
    }

    private boolean isPluginProject(GrailsProjectInfo info) {
        return existsFile(joinAsPath(info.getScmUrl(), toPluginFileName(info.getAppName())));
    }

    protected String toPluginFileName(String appName) {
        StringBuilder buff = new StringBuilder();
        for (String s : appName.split("-")) {
            buff.append(StringUtils.capitalize(s));
        }
        return buff.append("GrailsPlugin.groovy").toString();
    }

    protected String findLicenseLink(String text) {
        Matcher m = LICENSE.matcher(text);
        return m.find() ? m.group() : LISENCE_NA;
    }

    private GrailsProjectInfo createProjectInfo() {
        long start = System.currentTimeMillis();
        GrailsProjectInfo result = new GrailsProjectInfo();
        result.setName(name);
        result.setDomain("googlecode.com");
        result.setProjectUrl(format("http://code.google.com/p/%s/", name));
        try {
            String topPage = getFile(result.getProjectUrl()).getText();
            result.setLicenseLink(findLicenseLink(topPage));
        } catch (FileNotFoundException e) {
            context.getLogger().warn(e);
            result.setLicenseLink(LISENCE_NA);
        }
        SVNFIleInfo applicationProperties;
        try {
            applicationProperties = getFile(format("http://%s.googlecode.com/svn/trunk/application.properties", name));
        } catch (FileNotFoundException e) {
            SVNRepositoryWrapper repos = null;
            try {
                repos = new SVNRepositoryWrapper(format("http://%s.googlecode.com/svn", name));
                applicationProperties = repos.findFile("application.properties");
            } catch (FileNotFoundException ignore) {
                result.setErrorMessage("Not grails project.");
                result.setParseTime(System.currentTimeMillis() - start);
                return result;
            } finally {
                if (repos != null) repos.close();
            }
        }
        setupAppProperties(result, applicationProperties.getText());
        result.setScmUrl(substringBeforeLast(applicationProperties.getUrl(), "/").replaceAll("%20", " "));
        result.setType(isPluginProject(result) ? "grails-plugin" : "grails");
        if (!context.getGrailsMap().containsKey(result.getGrailsVersion())) {
            result.setErrorMessage(format("Unsupported grails version [%s].", result.getGrailsVersion()));
        }
        result.setParseTime(System.currentTimeMillis() - start);
        return result;
    }

    private void saveAsHudsonJob(GrailsProjectInfo info) throws Exception {
        Hudson hudson = Hudson.getInstance();
        String jobName = format("%s.%s", info.getName(), info.getDomain());

        FreeStyleProject job = (FreeStyleProject) hudson.getItem(jobName);
        if (job == null) {
            job = (FreeStyleProject) hudson.createProject(FreeStyleProject.DESCRIPTOR, jobName);
        }
        job.addProperty(new GoogleCodeProjectProperty(info.getProjectUrl()));

        job.setDescription(JOB_DESCRIPTION.generate(info));
        job.setLogRotator(new LogRotator(-1, 3));
        job.setScm(new SubversionSCM(Arrays.asList(new ModuleLocation(info.getScmUrl(), info.getName())), true,
                new GoogleCodeRepositoryBrowser(), ""));
        job.addTrigger(new SCMTrigger("*/5 * * * *"));
        job.setAssignedLabel(null);
        addGBuildWrapper(job);

        DescribableList<Builder,Descriptor<Builder>> builders = job.getBuildersList();
        builders.clear();
        builders.add(new Shell(JOB_SHELL.generate(info)));
        builders.add(new GrailsBuilder("clean test-app",
            context.getGrailsMap().get(info.getGrailsVersion()), null, null, null, null));

        DescribableList<Publisher,Descriptor<Publisher>> publishers = job.getPublishersList();
        publishers.clear();
        publishers.add(new JUnitResultArchiver(format("%s/test/reports/TEST*.xml", info.getName()), null));
        if (isActive("emotional-hudson")) {
            publishers.add(createEmotionalHudsonPublisher());
        }
        if (isActive("twitter")) {
            publishers.add(createTwitterPublisher());
        }

        job.save();
        job.onLoad(hudson, jobName);
    }

    private Publisher createEmotionalHudsonPublisher() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return (Publisher) Class.forName("hudson.plugins.emotional_hudson.EmotionalHudsonPublisher").newInstance();
    }

    @SuppressWarnings("unchecked")
    private Publisher createTwitterPublisher() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Class type = Class.forName("hudson.plugins.twitter.TwitterPublisher");
        Constructor c = type.getConstructor(String.class, String.class, Boolean.class, Boolean.class);
        return (Publisher) c.newInstance(null, null, false, false);
    }

    private boolean isActive(String shortName) {
        return Hudson.getInstance().getPlugin(shortName) != null;
    }

    @SuppressWarnings("unchecked")
    private void addGBuildWrapper(FreeStyleProject job) {
        try {
            ((DescribableList) F_BUILD_WRAPPERS.get(job)).add(new GBuildWrapper());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}