package com.zanox.hudson.plugins;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.tasks.Publisher;
import hudson.util.CopyOnWriteList;
import hudson.util.FormFieldValidator;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.jcraft.jsch.SftpException;

/**
 * <p>
 * This class implements the ftp publisher process by using the {@link FTPSite}.
 * </p>
 * <p>
 * HeadURL: $HeadURL:
 * http://z-bld-02:8080/zxdev/zxant_test_environment/trunk/formatting/codeTemplates.xml $<br />
 * Date: $Date: 2008-04-22 11:53:34 +0200 (Di, 22 Apr 2008) $<br />
 * Revision: $Revision: 2451 $<br />
 * </p>
 * 
 * @author $Author: ZANOX-COM\fit $
 * 
 */
public class FTPPublisher extends Publisher {

    /**
     * Hold an instance of the Descriptor implementation of this publisher.
     */
    public static final DescriptorImpl      DESCRIPTOR   = new DescriptorImpl();

    /**
     * This is a SimpleDateFormat instance to get a directory name which include a time stamp.
     */
    protected static final SimpleDateFormat ID_FORMATTER = new SimpleDateFormat(
                                                             "yyyy-MM-dd_HH-mm-ss");

    private String                          siteName;

    private final List<Entry>               entries      = new ArrayList<Entry>();

    /**
     * The default constructor.
     */
    public FTPPublisher() {
    }

    /**
     * The constructor which take a configured ftp site name to publishing the artifacts.
     * 
     * @param siteName the name of the ftp site configuration to use
     */
    public FTPPublisher(String siteName) {
        this.siteName = siteName;
    }

    /**
     * The getter for the entries field. (this field is set by the UI part of this plugin see
     * config.jelly file)
     * 
     * @return the value of the entries field
     */
    public List<Entry> getEntries() {
        return entries;
    }

    /**
     * This method returns the configured FTPSite object which match the siteName of the
     * FTPPublisher instance. (see Manage Hudson and System Configuration point FTP)
     * 
     * @return the matching FTPSite or null
     */
    public FTPSite getSite() {
        FTPSite[] sites = DESCRIPTOR.getSites();
        if (siteName == null && sites.length > 0) {
            // default
            return sites[0];
        }
        for (FTPSite site : sites) {
            if (site.getName().equals(siteName)) {
                return site;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @param build
     * @param launcher
     * @param listener
     * @return
     * @throws InterruptedException
     * @throws IOException {@inheritDoc}
     * @see hudson.tasks.BuildStep#perform(hudson.model.Build, hudson.Launcher,
     *      hudson.model.BuildListener)
     */
    public boolean perform(Build<?, ?> build, Launcher launcher, BuildListener listener)
        throws InterruptedException, IOException {
        if (build.getResult() == Result.FAILURE) {
            // build failed. don't post
            return true;
        }

        FTPSite ftpsite = null;
        try {
            ftpsite = getSite();
            listener.getLogger().println("Connecting to " + ftpsite.getHostname());
            ftpsite.createSession();

            URI workSpaceDir = build.getProject().getWorkspace().toURI();
            workSpaceDir = workSpaceDir.resolve(build.getProject().getName());

            Map<String, String> envVars = build.getEnvVars();
            String targetFolder = "/" + build.getProject().getName()
                + "/"
                + ID_FORMATTER.format(build.getTimestamp().getTime());
            ftpsite.mkdirs(targetFolder, listener.getLogger());

            long fileCount = 0;

            for (Entry e : entries) {
                String expanded = Util.replaceMacro(e.sourceFile, envVars);
                listener.getLogger().println(workSpaceDir);
                FilePath[] src = build.getProject().getWorkspace().list(expanded);
                String folderPath = Util.replaceMacro(e.filePath, envVars);
                if (src.length == 0) {
                    listener.getLogger().println("No file(s) found: " + expanded);
                }

                if (src.length == 1) {
                    ftpsite.mkdirs(folderPath, listener.getLogger());
                    ftpsite.upload(src[0], envVars, listener.getLogger());
                    fileCount++;
                } else {
                    String previosePath = "";
                    for (FilePath s : src) {

                        URI relativePath = workSpaceDir.relativize(s.toURI());
                        String relativePathString = relativePath.toString().substring(0,
                            relativePath.toString().lastIndexOf('/'));
                        if (!previosePath.equals(relativePathString)) {
                            ftpsite.changedToProjectRootDir(targetFolder, listener.getLogger());
                            ftpsite.mkdirs(relativePathString, listener.getLogger());
                            previosePath = relativePathString;
                        }
                        ftpsite.upload(s, envVars, listener.getLogger());
                        fileCount++;
                    }
                }
                ftpsite.changedToProjectRootDir(targetFolder, listener.getLogger());
            }
            listener.getLogger().println("transferred " + fileCount + " files");
        } catch (IOException e) {
            e.printStackTrace(listener.error("Failed to upload files"));
            build.setResult(Result.UNSTABLE);
        } catch (SftpException e) {
            e.printStackTrace();
        } finally {
            if (ftpsite != null) {
                ftpsite.closeSession();
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @return {@inheritDoc}
     * @see hudson.model.Describable#getDescriptor()
     */
    public Descriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

    /**
     * <p>
     * This class holds the metadata for the FTPPublisher.
     * </p>
     * 
     * @author $Author: ZANOX-COM\fit $
     * @see Descriptor
     */
    public static final class DescriptorImpl extends Descriptor<Publisher> {

        private final CopyOnWriteList<FTPSite> sites = new CopyOnWriteList<FTPSite>();

        /**
         * The default constructor.
         */
        public DescriptorImpl() {
            super(FTPPublisher.class);
            load();
        }

        /**
         * The name of the plugin to display them on the project configuration web page.
         * 
         * {@inheritDoc}
         * 
         * @return {@inheritDoc}
         * @see hudson.model.Descriptor#getDisplayName()
         */
        @Override
        public String getDisplayName() {
            return "Publish artifacts to FTP";
        }

        /**
         * Return the location of the help document for this publisher.
         * 
         * {@inheritDoc}
         * 
         * @return {@inheritDoc}
         * @see hudson.model.Descriptor#getHelpFile()
         */
        @Override
        public String getHelpFile() {
            return "/plugin/ftppublisher/help.html";
        }

        /**
         * This method is called by hudson if the user has clicked the add button of the FTP
         * repository hosts point in the System Configuration web page. It's create a new instance
         * of the {@link FTPPublisher} class and added all configured ftp sites to this instance by
         * calling the method {@link FTPPublisher#getEntries()} and on it's return value the addAll
         * method is called.
         * 
         * {@inheritDoc}
         * 
         * @param req {@inheritDoc}
         * @return {@inheritDoc}
         * @see hudson.model.Descriptor#newInstance(org.kohsuke.stapler.StaplerRequest)
         */
        @Override
        public Publisher newInstance(StaplerRequest req) {
            FTPPublisher pub = new FTPPublisher();
            req.bindParameters(pub, "ftp.");
            pub.getEntries().addAll(req.bindParametersToList(Entry.class, "ftp.entry."));
            return pub;
        }

        /**
         * The getter of the sites field.
         * 
         * @return the value of the sites field.
         */
        public FTPSite[] getSites() {
            Iterator<FTPSite> it = sites.iterator();
            int size = 0;
            while (it.hasNext()) {
                it.next();
                size++;
            }
            return sites.toArray(new FTPSite[size]);
        }

        /**
         * {@inheritDoc}
         * 
         * @param req {@inheritDoc}
         * @return {@inheritDoc}
         * @see hudson.model.Descriptor#configure(org.kohsuke.stapler.StaplerRequest)
         */
        @Override
        public boolean configure(StaplerRequest req) {
            sites.replaceBy(req.bindParametersToList(FTPSite.class, "ftp."));
            save();
            return true;
        }

        /**
         * This method validates the current entered ftp configuration data. That is made by create
         * a ftp connection.
         * 
         * @param req the current {@link javax.servlet.http.HttpServletRequest}
         * @param rsp the current {@link javax.servlet.http.HttpServletResponse}
         * @throws IOException is able to be thrown if a error by creating the ftp connection occurs 
         * @throws ServletException is able to be thrown if a error by creating the ftp connection occurs
         */
        public void doLoginCheck(final StaplerRequest req, StaplerResponse rsp)
            throws IOException, ServletException {
            new FormFieldValidator(req, rsp, false) {

                /**
                 * {@inheritDoc}
                 * 
                 * @throws IOException {@inheritDoc}
                 * @throws ServletException {@inheritDoc}
                 * @see hudson.util.FormFieldValidator#check()
                 */
                @Override
                protected void check()
                    throws IOException, ServletException {
                    String hostname = Util.fixEmpty(request.getParameter("hostname"));
                    if (hostname == null) { // hosts is not entered yet
                        ok();
                        return;
                    }
                    FTPSite site = new FTPSite(hostname, request.getParameter("port"), request
                        .getParameter("timeOut"), request.getParameter("user"), request
                        .getParameter("pass"));
                    try {

                        site.createSession();
                        site.closeSession();

                        ok();
                    } catch (Exception e) {
                        error(e.getMessage());
                    }
                }
            } .process();
        }
    }
}
