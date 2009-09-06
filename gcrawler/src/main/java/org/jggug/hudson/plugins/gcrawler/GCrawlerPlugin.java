package org.jggug.hudson.plugins.gcrawler;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.Hudson;

@Extension
public class GCrawlerPlugin extends Plugin {

    private GCrawlerConfig config;

    @Override
    public void start() throws Exception {
        load();
    }

    @Override
    public void stop() throws Exception {
        save();
    }

    public static GCrawlerConfig getConfig() {
        GCrawlerPlugin plugin = getInstance();
        if (plugin.config == null) {
            plugin.config = new GCrawlerConfig();
        }
        return plugin.config;
    }

    public static GCrawlerPlugin getInstance() {
        return Hudson.getInstance().getPlugin(GCrawlerPlugin.class);
    }
}
