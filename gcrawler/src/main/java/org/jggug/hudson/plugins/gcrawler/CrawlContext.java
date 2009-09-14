package org.jggug.hudson.plugins.gcrawler;

import static org.jggug.hudson.plugins.gcrawler.util.PropertyFileUtils.getStringPropertyValue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.g2one.hudson.grails.GrailsBuilder;
import com.g2one.hudson.grails.GrailsInstallation;

public class CrawlContext {

    private Map<String, String> grailsMap;

    private Map<String, GrailsProjectInfo> projectMap;

    private CrawlLogger logger;

    private boolean isClosed = true;

    private File logFile;

    public Map<String, String> getGrailsMap() {
        return grailsMap;
    }

    public CrawlLogger getLogger() {
        return logger;
    }

    public void setGrailsMap(Map<String, String> grailsMap) {
        this.grailsMap = grailsMap;
    }

    public void setLogger(CrawlLogger logger) {
        this.logger = logger;
    }

    public void close() {
        isClosed = true;
        logger.close();
    }

    public boolean isClosed() {
        return isClosed;
    }

    public File getLogFile() {
        return logFile;
    }

    public static CrawlContext newInstance() {
        CrawlContext ctx = new CrawlContext();
        File logFile;
        try {
            logFile = File.createTempFile("gcrawler", ".log");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logFile.deleteOnExit();
        ctx.logFile = logFile;
        ctx.logger = new CrawlLogger(logFile);
        ctx.grailsMap = mapGrails();
        ctx.projectMap = mapProject();
        ctx.isClosed = false;
        return ctx;
    }

    private static Map<String, GrailsProjectInfo> mapProject() {
        Map<String, GrailsProjectInfo> result = new HashMap<String, GrailsProjectInfo>();
        for (GrailsProjectInfo info : GCrawlerPlugin.getConfig().getGrailsProjectInfoList()) {
            result.put(info.getName(), info);
        }
        return result;
    }

    private static Map<String, String> mapGrails() {
        Map<String, String> result = new HashMap<String, String>();
        for (GrailsInstallation inst : GrailsBuilder.DESCRIPTOR.getInstallations()) {
            if (inst.getExists()) {
                String version = getStringPropertyValue(
                    new File(inst.getGrailsHome(), "build.properties"), "grails.version");
                result.put(version, inst.getName());
            }
        }
        return result;
    }

    public Map<String, GrailsProjectInfo> getProjectMap() {
        return projectMap;
    }

    public void setProjectMap(Map<String, GrailsProjectInfo> projectMap) {
        this.projectMap = projectMap;
    }
}
