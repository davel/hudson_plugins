package com.zanox.hudson.plugins;

import hudson.Plugin;
import hudson.tasks.BuildStep;

/**
 * <p>
 * This class extends from the {@link Plugin} class and register the FTPPlugin as publisher plugin
 * in hudson.
 * </p>
 * <p>
 * HeadURL: $HeadURL:
 * http://z-bld-02:8080/zxdev/zxapp_hudson_ftp_plugin/trunk/src/main/java/com/zanox/hudson/plugins/PluginImpl.java $<br />
 * Date: $Date: 2008-04-22 11:51:22 +0200 (Di, 22 Apr 2008) $<br />
 * Revision: $Revision: 2446 $<br />
 * </p>
 * 
 * @author $Author: ZANOX-COM\fit $
 * 
 */
public class PluginImpl extends Plugin {

    /**
     * {@inheritDoc}
     * 
     * @throws Exception {@inheritDoc}
     * @see hudson.Plugin#start()
     */
    @Override
    public void start()
        throws Exception {
        BuildStep.PUBLISHERS.add(FTPPublisher.DESCRIPTOR);
    }
}
