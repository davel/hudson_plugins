package hudson.plugins.bamboo;

/*
 * (c) Copyright WesternGeco. Unpublished work, created 2009. All rights
 * reserved under copyright laws. This information is confidential and is
 * the trade property of WesternGeco. Do not use, disclose, or reproduce
 * without the prior written permission of the owner.
 *
 * File created: 30. des. 2009 14.08.01
 */

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author ANilsen
 * 
 */
public class BambooNotifier extends Notifier {

    private final String jobName;
    private final String serverAddress;
    private final String username;
    private final String password;
    private final boolean triggerUnstable;

    /**
     * @return the jobName
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * @return the serverAddress
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the triggerUnstable
     */
    public boolean isTriggerUnstable() {
        return triggerUnstable;
    }

    @DataBoundConstructor
    public BambooNotifier(String jobName, String serverAddress,
            String username, String password, boolean triggerUnstable) {
        if (serverAddress.endsWith("/"))
            this.serverAddress = serverAddress;
        else
            this.serverAddress = serverAddress + "/";
        this.jobName = jobName;
        this.username = username;
        this.password = password;
        this.triggerUnstable = triggerUnstable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see hudson.tasks.BuildStep#getRequiredMonitorService()
     */
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.AbstractBuild
     * , hudson.Launcher, hudson.model.BuildListener)
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
            BuildListener listener) throws InterruptedException, IOException {
        if (build.getResult() == Result.SUCCESS)
            triggerBamboo();
        else if (triggerUnstable && build.getResult() == Result.UNSTABLE) {
            System.out.println("Triggering unstable build");
            triggerBamboo();
        }
        return true;
    }

    void triggerBamboo() {
        String url = serverAddress + "rest/api/latest/queue/" + jobName
                + "?os_authType=basic";
        PostMethod postMethod = new PostMethod(url);
        HttpClient client = new HttpClient();
        Credentials credentials = new UsernamePasswordCredentials(username,
                password);
        client.getState().setCredentials(AuthScope.ANY, credentials);
        try {
            int status = client.executeMethod(postMethod);
            System.out.printf("Response code from %s: %d%n", url, status);
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            postMethod.releaseConnection();
        }
    }

    @Extension
    public static final class DescriptorImpl extends
            BuildStepDescriptor<Publisher> {

        /*
         * (non-Javadoc)
         * 
         * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
         */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see hudson.model.Descriptor#getDisplayName()
         */
        @Override
        public String getDisplayName() {
            return "Bamboo Notifier";
        }

    }
}
