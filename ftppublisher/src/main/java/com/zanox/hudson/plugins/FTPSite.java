package com.zanox.hudson.plugins;

import hudson.FilePath;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;

import com.jcraft.jsch.SftpException;

/**
 * <p>
 * This class
 * </p>
 * <p>
 * HeadURL: $HeadURL:
 * http://z-bld-02:8080/zxdev/zxant_test_environment/trunk/formatting/codeTemplates.xml $<br />
 * Date: $Date: 2008-04-22 11:53:34 +0200 (Di, 22 Apr 2008) $<br />
 * Revision: $Revision: 2451 $<br />
 * </p>
 * 
 * @author $Author: ZANOX-COM\fit $
 */
public class FTPSite {

    /** The Constant DEFAULT_FTP_PORT. */
    private static final int DEFAULT_FTP_PORT = 21;

    /** The hostname. */
    private String           hostname;

    /** The time out. */
    private int              timeOut;

    /** The port. */
    private int              port;

    /** The username. */
    private String           username;

    /** The password. */
    private String           password;

    /** The ftp dir. */
    private String           ftpDir;

    /** The ftp client. */
    private FTPClient        ftpClient;

    /**
     * Instantiates a new FTP site.
     */
    public FTPSite() {

    }

    /**
     * Instantiates a new FTP site.
     * 
     * @param hostname the hostname
     * @param port the port
     * @param timeOut the time out
     * @param username the username
     * @param password the password
     * @param ftpDir the ftp dir
     */
    public FTPSite(String hostname, int port, int timeOut, String username, String password,
        String ftpDir) {
        this.hostname = hostname;
        this.port = port;
        this.timeOut = timeOut;
        this.username = username;
        this.password = password;
        this.ftpDir = ftpDir;
    }

    /**
     * Instantiates a new FTP site.
     * 
     * @param hostname the hostname
     * @param port the port
     * @param timeOut the time out
     * @param username the username
     * @param password the password
     */
    public FTPSite(String hostname, String port, String timeOut, String username, String password) {
        this.hostname = hostname;
        try {
            this.port = Integer.parseInt(port);
            this.timeOut = Integer.parseInt(timeOut);
        } catch (Exception e) {
            this.port = DEFAULT_FTP_PORT;
        }
        this.username = username;
        this.password = password;
    }

    /**
     * Gets the time out.
     * 
     * @return the time out
     */
    public int getTimeOut() {
        return timeOut;
    }

    /**
     * Sets the time out.
     * 
     * @param timeOut the new time out
     */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    /**
     * Gets the hostname.
     * 
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the hostname.
     * 
     * @param hostname the new hostname
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Gets the port.
     * 
     * @return the port
     */
    public String getPort() {
        return "" + port;
    }

    /**
     * Sets the port.
     * 
     * @param port the new port
     */
    public void setPort(String port) {
        if (port != null) {
            try {
                this.port = Integer.parseInt(port);
            } catch (NumberFormatException e) {
                this.port = DEFAULT_FTP_PORT;
            }
        } else {
            this.port = DEFAULT_FTP_PORT;
        }

    }

    /**
     * Gets the integer port.
     * 
     * @return the integer port
     */
    public int getIntegerPort() {
        return port;
    }

    /**
     * Gets the username.
     * 
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     * 
     * @param username the new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password.
     * 
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     * 
     * @param password the new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the ftp dir.
     * 
     * @return the ftp dir
     */
    public String getFtpDir() {
        return ftpDir;
    }

    /**
     * Sets the ftp dir.
     * 
     * @param rootRepositoryPath the new ftp dir
     */
    public void setFtpDir(String rootRepositoryPath) {
        this.ftpDir = rootRepositoryPath;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return hostname;
    }

    /**
     * This method open an ftp connection and login with the specified user name and password.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void createSession()
        throws IOException {
        ftpClient = new FTPClient();
        ftpClient.setDefaultTimeout(timeOut);
        ftpClient.connect(hostname, port);
        // ftpClient.setSoTimeout(timeout)
        ftpClient.login(username, password);
        ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
        if (!ftpClient.changeWorkingDirectory(ftpDir)) {
            throw new IOException("Can't get stat of root ftp directory:" + ftpDir);
        }
    }

    /**
     * This method disconnect the current ftp connection.
     */
    public void closeSession() {
        if (ftpClient != null) {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ftpClient = null;
        }
    }

    /**
     * Changed to project root dir.
     * 
     * @param projectRootDir the project root dir
     * @param logger the logger
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void changedToProjectRootDir(String projectRootDir, PrintStream logger)
        throws IOException {
        if (!ftpClient.changeWorkingDirectory("/" + ftpDir + "/" + projectRootDir)) {
            logger.println("error by changing into the " + ftpDir + "/" + projectRootDir);
        }
        logger.println("current root dir " + ftpClient.printWorkingDirectory());
    }

    public void upload(FilePath filePath, Map<String, String> envVars, PrintStream logger)
        throws IOException, InterruptedException, SftpException {

        if (ftpClient == null) {
            throw new IOException("Connection to " + hostname
                + ", user="
                + username
                + " is not established");
        }

        if (filePath.isDirectory()) {
            FilePath[] subfiles = filePath.list("**/*");
            if (subfiles != null) {
                for (int i = 0; i < subfiles.length; i++) {
                    upload(subfiles[i], envVars, logger);
                }
            }
        } else {
            String localfilename = filePath.getName();
            // mkdirs(folderPath, logger);
            InputStream in = filePath.read();
            ftpClient.storeFile(localfilename, in);
            in.close();
        }

    }

    /**
     * Mkdirs.
     * 
     * @param filePath the file path
     * @param logger the logger
     * 
     * @throws SftpException the sftp exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void mkdirs(String filePath, PrintStream logger)
        throws SftpException, IOException {
        String[] pathnames = filePath.split("/");
        String curdir = ftpDir;
        if (pathnames != null) {
            for (int i = 0; i < pathnames.length; i++) {
                if (pathnames[i].length() == 0) {
                    continue;
                }

                if (!ftpClient.changeWorkingDirectory(curdir + "/" + pathnames[i])) {
                    // try to create dir
                    // logger.println("Trying to create " + curdir + "/" + pathnames[i]);
                    ftpClient.mkd(pathnames[i]);
                }
                curdir = curdir + "/" + pathnames[i];
                ftpClient.changeWorkingDirectory(pathnames[i]);
            }
        }
    }
}
