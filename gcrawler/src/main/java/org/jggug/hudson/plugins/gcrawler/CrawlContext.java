package org.jggug.hudson.plugins.gcrawler;

import static org.jggug.hudson.plugins.gcrawler.util.PropertyFileUtils.getStringPropertyValue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.g2one.hudson.grails.GrailsBuilder;
import com.g2one.hudson.grails.GrailsInstallation;

public class CrawlContext {

    private Map<String, String> grailsMap;

    private CrawlLogger logger;

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

    public static CrawlContext newInstance(File logFile) {
        CrawlContext ctx = new CrawlContext();
        ctx.logger = new CrawlLogger(logFile);
        ctx.grailsMap = mapGrails();
        return ctx;
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
}
