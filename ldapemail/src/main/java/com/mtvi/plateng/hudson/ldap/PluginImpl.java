/**
 * Copyright (c) 2008, MTV Networks
 */

package com.mtvi.plateng.hudson.ldap;

import hudson.Plugin;
import hudson.Util;
import hudson.XmlFile;
import hudson.model.Descriptor.FormException;
import hudson.model.Hudson;
import hudson.tasks.MailAddressResolver;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import net.sf.json.JSONObject;

/**
 * Entry point of for the LDAP Email plugin. Loads configuration from
 * com.mtvi.plateng.hudson.ldap.LdapMailAddressResolver.xml and adds an instance
 * of LdapMailAddressResolver to the MailAddressResolver list.
 * 
 * @author justinedelson
 */
public class PluginImpl extends Plugin {

    /**
     * A logger object.
     */
    private static final Logger LOGGER = Logger.getLogger("hudson." + PluginImpl.class.getName());

    public Configuration config;

    /**
     * Plugin lifecycle method. Loads configuration and adds configured instance
     * of LdapMailAddressResolver to MailAddressResolver list.
     * 
     * @see hudson.Plugin#start()
     * @throws Exception if something goes wrong
     */
    @Override
    public void start() throws Exception {
        config = loadConfiguration();
        MailAddressResolver.LIST.add(new LdapMailAddressResolver(config));
    }

    /**
     * Loads confiugration file from
     * com.mtvi.plateng.hudson.ldap.LdapMailAddressResolver.xml.
     * 
     * @return a Configuration object, populated from the file, if it exists
     * @throws IOException if the file can't be read.
     */
    protected Configuration loadConfiguration() throws IOException {
        XmlFile xmlFile = getConfigXml();
        Configuration config = null;
        if (xmlFile.exists()) {
            config = (Configuration) xmlFile.read();
            LOGGER.info(String.format("Loaded configuration data: %s", config.toString()));
        } else {
            LOGGER.info("Could not find configuration file, creating empty object");
            config = new Configuration();
        }
        return config;
    }

    @Override
    public void configure(JSONObject formData) throws IOException {
        config.setServer(Util.fixEmptyAndTrim(formData.optString("server")));
        config.setBaseDN(Util.fixEmptyAndTrim(formData.optString("baseDN")));
        config.setBindDN(Util.fixEmptyAndTrim(formData.optString("bindDN")));
        config.setBindPassword(Util.fixEmptyAndTrim(formData.optString("bindPassword")));
        config.setEmailAttribute(Util.fixEmptyAndTrim(formData.optString("emailAttribute")));
        config.setSearchAttribute(Util.fixEmptyAndTrim(formData.optString("searchAttribute")));
        config.setPerformSearch(formData.optBoolean("performSearch", false));
        getConfigXml().write(config);
    }

    @Override
    protected XmlFile getConfigXml() {
        return new XmlFile(Hudson.XSTREAM,
                           new File(Hudson.getInstance().getRootDir(),
                                    LdapMailAddressResolver.class.getName() + ".xml"));
    }
}
