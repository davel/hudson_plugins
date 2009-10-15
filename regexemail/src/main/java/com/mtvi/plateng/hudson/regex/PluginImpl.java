/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.regex;

import hudson.Plugin;
import hudson.XmlFile;
import hudson.model.Hudson;
import hudson.tasks.MailAddressResolver;

import java.io.File;
import java.io.IOException;

/**
 * Entry point of for the Regex Email plugin. Loads configuration from
 * com.mtvi.plateng.hudson.ldap.RegexMailAddressResolver.xml and adds an
 * instance of RegexMailAddressResolver to the MailAddressResolver list.
 * 
 * @author justinedelson
 */
public class PluginImpl extends Plugin {

    /**
     * Plugin lifecycle method. Loads configuration and adds configured instance
     * of RegexMailAddressResolver to MailAddressResolver list.
     * 
     * @see hudson.Plugin#start()
     * @throws Exception
     *             if something goes wrong
     */
    @Override
    public void start() throws Exception {
        IConfiguration config = loadConfiguration();
        MailAddressResolver.all().add(new RegexMailAddressResolver(config));
    }

    /**
     * Loads confiugration file from
     * com.mtvi.plateng.hudson.regex.RegexMailAddressResolver.xml.
     * 
     * @return a Configuration object, populated from the file, if it exists
     * @throws IOException
     *             if the file can't be read.
     */
    protected static IConfiguration loadConfiguration() throws IOException {
        Hudson hudson = Hudson.getInstance();
        File rootDirectory = hudson.getRootDir();
        String fileName = RegexMailAddressResolver.class.getName() + ".xml";
        XmlFile xmlFile = new XmlFile(Hudson.XSTREAM, new File(rootDirectory, fileName));
        IConfiguration config = null;
        if (xmlFile.exists()) {
            config = (IConfiguration) xmlFile.read();
        } else {
            config = new Configuration();
        }
        return config;
    }
}
