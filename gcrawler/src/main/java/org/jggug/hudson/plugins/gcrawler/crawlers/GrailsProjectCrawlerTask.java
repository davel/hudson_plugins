package org.jggug.hudson.plugins.gcrawler.crawlers;

import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace;
import static org.jggug.hudson.plugins.gcrawler.util.HudsonPluginUtils.addGBuildWrapper;
import static org.jggug.hudson.plugins.gcrawler.util.HudsonPluginUtils.createEmotionalHudsonPublisher;
import static org.jggug.hudson.plugins.gcrawler.util.HudsonPluginUtils.createTwitterPublisher;
import static org.jggug.hudson.plugins.gcrawler.util.HudsonPluginUtils.isActive;
import static org.jggug.hudson.plugins.gcrawler.util.JobTemplate.createTemplate;
import static org.jggug.hudson.plugins.gcrawler.util.PropertyFileUtils.getStringPropertyValue;
import static org.jggug.hudson.plugins.gcrawler.util.PropertyFileUtils.toResourceBundleFromText;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.tasks.Builder;
import hudson.tasks.LogRotator;
import hudson.tasks.Publisher;
import hudson.tasks.Shell;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.triggers.SCMTrigger;
import hudson.util.DescribableList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jggug.hudson.plugins.gcrawler.CrawlContext;
import org.jggug.hudson.plugins.gcrawler.GrailsProjectInfo;
import org.jggug.hudson.plugins.gcrawler.scm.FileInfo;
import org.jggug.hudson.plugins.gcrawler.scm.RepositoryException;
import org.jggug.hudson.plugins.gcrawler.scm.RepositoryWrapper;
import org.jggug.hudson.plugins.gcrawler.util.HttpUtils;
import org.jggug.hudson.plugins.gcrawler.util.JobTemplate;

import com.g2one.hudson.grails.GrailsBuilder;

public abstract class GrailsProjectCrawlerTask implements Callable<GrailsProjectInfo> {

    private static final String VERSION = "app.version";

    private static final String SERVLET_VERSION = "app.servlet.version";

    private static final String GRAILS_VERSION = "app.grails.version";

    private static final String NAME = "app.name";

    private static final Pattern PLUGINS = compile("plugins\\.(.*?)=(.*)");

    private static final JobTemplate JOB_SHELL = createTemplate("google_grails_shell.txt");

    private static final Pattern PATTERN_TESTS = Pattern.compile("^.*Tests.groovy$");

    private String name;

    private CrawlContext context;

    private JobTemplate descriptionTemplate;

    private RepositoryWrapper repository;

    public GrailsProjectCrawlerTask(String name, CrawlContext context, JobTemplate template, RepositoryWrapper repository) {
        this.name = name;
        this.context = context;
        this.descriptionTemplate = template;
        this.repository = repository;
    }

    public GrailsProjectInfo call() throws Exception {
        long start = System.currentTimeMillis();
        GrailsProjectInfo oldProject = context.getProjectMap().get(name);
        GrailsProjectInfo result = null;
        if (oldProject != null) {
            if (isUpdated(oldProject)) {
                result = createProjectInfo();
            } else {
                context.getLogger().info("--++-- not modified! --++--");
                return oldProject;
            }
            if (result.hasError()) {
                removeHudsonJob(result);
                return result;
            }
        } else {
            result = createProjectInfo();
        }
        result.setParseTime(System.currentTimeMillis() - start);
        context.getLogger().info(result.toString());
        return result;
    }

    protected boolean isUpdated(GrailsProjectInfo oldProject) throws RepositoryException {
        return oldProject.getRevision() != repository.getLatestRevision();
    }

    private GrailsProjectInfo createProjectInfo() {
        GrailsProjectInfo info = new GrailsProjectInfo();
        info.setName(name);
        info.setProjectUrl(getProjectUrl(info));
        info.setDomain(getDomain());
        try {
            info.setRevision(repository.getLatestRevision());
            FileInfo appProperties = repository.findFile("application.properties");
            info.setTestsAvirable(repository.existsFileByPattern(PATTERN_TESTS));
            String url = appProperties.getUrl();
            info.setScmUrl(url.substring(0, url.lastIndexOf('/')));
            setupProjectInfo(info, appProperties.getContent());
            info.setType(isPluginProject(info) ? "grails-plugin" : "grails");
            if (!info.hasError()) saveAsHudsonJob(info);
        } catch (FileNotFoundException e) {
            info.setErrorMessage("application.properties is not found.");
        } catch (Exception e) {
            context.getLogger().info(getFullStackTrace(e));
            info.setErrorMessage("Unknwon erorr.");
        }
        return info;
    }

    protected void setupProjectInfo(GrailsProjectInfo info, String appProperties) {
        try {
            PropertyResourceBundle b = toResourceBundleFromText(appProperties);
            info.setAppName(getStringPropertyValue(b, NAME));
            info.setVersion(getStringPropertyValue(b, VERSION));
            info.setGrailsVersion(getStringPropertyValue(b, GRAILS_VERSION));
            info.setServletVersion(getStringPropertyValue(b, SERVLET_VERSION));
            Matcher m = PLUGINS.matcher(appProperties);
            while (m.find()) {
                info.addPlugin(format("%s-%s", m.group(1), m.group(2)));
            }
            if (!context.getGrailsMap().containsKey(info.getGrailsVersion())) {
                info.setErrorMessage(format("Unsupported grails version. [%s]", info.getGrailsVersion()));
            }
        } catch (IOException e) {
            info.setErrorMessage("invalid application.properties.");
        }
    }

    protected void removeHudsonJob(GrailsProjectInfo info) throws InterruptedException, IOException {
        FreeStyleProject job = (FreeStyleProject) Hudson.getInstance().getItem(info.toJobName());
        if (job != null) {
            job.delete();
        }
    }

    protected void saveAsHudsonJob(GrailsProjectInfo info) throws Exception {
        String jobName = info.toJobName();
        Hudson hudson = Hudson.getInstance();
        FreeStyleProject job = (FreeStyleProject) hudson.getItem(jobName);
        if (job == null) {
            job = (FreeStyleProject) hudson.createProject(FreeStyleProject.DESCRIPTOR, jobName);
        }
        setupJob(job, info);
        job.save();
        job.onLoad(hudson, jobName);
    }

    protected void setupJob(FreeStyleProject job, GrailsProjectInfo info) throws Exception {
        job.setDescription(descriptionTemplate.generate(info));
        job.setLogRotator(new LogRotator(-1, 3));
        job.addTrigger(new SCMTrigger("*/5 * * * *"));
        job.setAssignedLabel(null);
        addGBuildWrapper(job);

        DescribableList<Builder,Descriptor<Builder>> builders = job.getBuildersList();
        builders.clear();
        builders.add(new Shell(JOB_SHELL.generate(info)));
        String targets = "clean " + (info.isTestsAvirable() ? "test-app" : "package") + " --non-interactive";
        builders.add(new GrailsBuilder(targets,
            context.getGrailsMap().get(info.getGrailsVersion()), null, null, null, null));

        DescribableList<Publisher,Descriptor<Publisher>> publishers = job.getPublishersList();
        publishers.clear();
        if (info.isTestsAvirable()) {
            publishers.add(new JUnitResultArchiver(format("%s/test/reports/TEST*.xml", info.getName()), null));
        }
        if (isActive("emotional-hudson")) {
            publishers.add(createEmotionalHudsonPublisher());
        }
        if (isActive("twitter")) {
            publishers.add(createTwitterPublisher());
        }
    }

    protected abstract String getProjectUrl(GrailsProjectInfo info);

    protected abstract String getDomain();

    protected boolean isPluginProject(GrailsProjectInfo info) {
        return HttpUtils.existsFile(HttpUtils.joinAsPath(info.getScmUrl(), toPluginFileName(info.getAppName())));
    }

    protected String toPluginFileName(String appName) {
        StringBuilder buff = new StringBuilder();
        for (String s : appName.split("-")) {
            buff.append(StringUtils.capitalize(s));
        }
        return buff.append("GrailsPlugin.groovy").toString();
    }

}
