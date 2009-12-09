package hudson.plugins.sshslaves;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import static java.util.logging.Level.FINE;
import java.net.URL;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;
import hudson.util.IOException2;
import hudson.util.StreamCopyThread;
import hudson.util.StreamTaskListener;
import hudson.Extension;
import hudson.AbortException;
import hudson.Util;
import hudson.tools.JDKInstaller;
import hudson.tools.JDKInstaller.Platform;
import hudson.tools.JDKInstaller.CPU;
import static hudson.Util.fixEmpty;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.putty.PuTTYKey;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.io.output.CountingOutputStream;

/**
 * A computer launcher that tries to start a linux slave by opening an SSH connection and trying to find java.
 */
public class SSHLauncher extends ComputerLauncher {

    /**
     * Field host
     */
    private final String host;

    /**
     * Field port
     */
    private final int port;

    /**
     * Field username
     */
    private final String username;

    /**
     * Field password
     *
     * @todo remove password once authentication is stored in the descriptor.
     */
    private final String password;

    /**
     * Field privatekey
     */
    private final String privatekey;

    /**
     * Field jvmOptions.
     */
    private final String jvmOptions;

    /**
     * Field connection
     */
    private transient Connection connection;

    /**
     * Constructor SSHLauncher creates a new SSHLauncher instance.
     *
     * @param host       The host to connect to.
     * @param port       The port to connect on.
     * @param username   The username to connect as.
     * @param password   The password to connect with.
     * @param privatekey The ssh privatekey to connect with.
     * @param jvmOptions
     */
    @DataBoundConstructor
    public SSHLauncher(String host, int port, String username, String password, String privatekey, String jvmOptions) {
        this.host = host;
        this.jvmOptions = jvmOptions;
        this.port = port == 0 ? 22 : port;
        this.username = username;
        this.password = password;
        this.privatekey = privatekey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLaunchSupported() {
        return true;
    }

    /**
     * Gets the JVM Options used to launch the slave JVM.
     * @return
     */
    public String getJvmOptions() {
        return jvmOptions == null ? "" : jvmOptions;
    }

    /**
     * Gets the formatted current time stamp.
     *
     * @return the formatted current time stamp.
     */
    private static String getTimestamp() {
        return String.format("[%1$tD %1$tT]", new Date());
    }

    /**
     * Returns the remote root workspace (without trailing slash).
     *
     * @param computer The slave computer to get the root workspace of.
     *
     * @return the remote root workspace (without trailing slash).
     */
    private static String getWorkingDirectory(SlaveComputer computer) {
        String workingDirectory = computer.getNode().getRemoteFS();
        while (workingDirectory.endsWith("/")) {
            workingDirectory = workingDirectory.substring(0, workingDirectory.length() - 1);
        }
        return workingDirectory;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void launch(final SlaveComputer computer, final TaskListener listener) throws InterruptedException {
        connection = new Connection(host, port);
        try {
            openConnection(listener);

            verifyNoHeaderJunk(listener);
            reportEnvironment(listener);

            String java = null;
            List<String> tried = new ArrayList<String>();
            outer:
            for (JavaProvider provider : JavaProvider.all()) {
                for (String javaCommand : provider.getJavas(computer, listener, connection)) {
                    LOGGER.fine("Trying Java at "+javaCommand);
                    try {
                        tried.add(javaCommand);
                        java = checkJavaVersion(listener, javaCommand);
                        if (java != null) {
                            break outer;
                        }
                    } catch (IOException e) {
                        LOGGER.log(FINE, "Failed to check the Java version",e);
                        // try the next one
                    }
                }
            }

            final String workingDirectory = getWorkingDirectory(computer);

            if (java == null) {
                // attempt auto JDK installation
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                try {
                    java = attemptToInstallJDK(listener, workingDirectory, buf);
                } catch (IOException e) {
                    throw new IOException2("Could not find any known supported java version in "+tried+", and we also failed to install JDK as a fallback",e);
                }
            }


            copySlaveJar(listener, workingDirectory);

            startSlave(computer, listener, java, workingDirectory);

            PluginImpl.register(connection);
        } catch (RuntimeException e) {
            e.printStackTrace(listener.error(Messages.SSHLauncher_UnexpectedError()));
        } catch (Error e) {
            e.printStackTrace(listener.error(Messages.SSHLauncher_UnexpectedError()));
        } catch (IOException e) {
            e.printStackTrace(listener.getLogger());
            connection.close();
            connection = null;
            listener.getLogger().println(Messages.SSHLauncher_ConnectionClosed(getTimestamp()));
        }
    }

    /**
     * Makes sure that SSH connection won't produce any unwanted text, which will interfere with sftp execution.
     */
    private void verifyNoHeaderJunk(TaskListener listener) throws IOException, InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        connection.exec("true",baos);
        String s = baos.toString();
        if (s.length()!=0) {
            listener.getLogger().println(Messages.SSHLauncher_SSHHeeaderJunkDetected());
            listener.getLogger().println(s);
            throw new AbortException();
        }
    }

    /**
     * Attempts to install JDK, and return the path to Java.
     */
    private String attemptToInstallJDK(TaskListener listener, String workingDirectory, ByteArrayOutputStream buf) throws IOException, InterruptedException {
        if (connection.exec("uname -a",new TeeOutputStream(buf,listener.getLogger()))!=0)
            throw new IOException("Failed to run 'uname' to obtain the environment");

        // guess the platform from uname output. I don't use the specific options because I'm not sure
        // if various platforms have the consistent options
        //
        // === some of the output collected ====
        // Linux bear 2.6.28-15-generic #49-Ubuntu SMP Tue Aug 18 19:25:34 UTC 2009 x86_64 GNU/Linux
        // Linux wssqe20 2.6.24-24-386 #1 Tue Aug 18 16:24:26 UTC 2009 i686 GNU/Linux
        // SunOS hudson 5.11 snv_79a i86pc i386 i86pc
        // SunOS legolas 5.9 Generic_112233-12 sun4u sparc SUNW,Sun-Fire-280R
        // CYGWIN_NT-5.1 franz 1.7.0(0.185/5/3) 2008-07-22 19:09 i686 Cygwin
        // Windows_NT WINXPIE7 5 01 586
        //        (this one is from MKS)

        String uname = buf.toString();
        Platform p = null;
        CPU cpu = null;
        if (uname.contains("GNU/Linux"))        p = Platform.LINUX;
        if (uname.contains("SunOS"))            p = Platform.SOLARIS;
        if (uname.contains("CYGWIN"))           p = Platform.WINDOWS;
        if (uname.contains("Windows_NT"))       p = Platform.WINDOWS;

        if (uname.contains("sparc"))            cpu = CPU.Sparc;
        if (uname.contains("x86_64"))           cpu = CPU.amd64;
        if (Pattern.compile("\\bi?[3-6]86\\b").matcher(uname).find())           cpu = CPU.i386;  // look for ix86 as a word

        if (p==null || cpu==null)
            throw new IOException(Messages.SSHLauncher_FailedToDetectEnvironment(uname));

        String javaDir = workingDirectory + "/jdk"; // this is where we install Java to
        String bundleFile = workingDirectory + "/" + p.bundleFileName; // this is where we download the bundle to

        SFTPClient sftp = new SFTPClient(connection);
        // wipe out and recreate the Java directory
        connection.exec("rm -rf "+javaDir,listener.getLogger());
        sftp.mkdirs(javaDir, 0755);

        JDKInstaller jdk = new JDKInstaller("jdk-6u16-oth-JPR@CDS-CDS_Developer",true);
        URL bundle = jdk.locate(listener, p, cpu);

        listener.getLogger().println("Downloading JDK6u16");
        Util.copyStreamAndClose(bundle.openStream(),new BufferedOutputStream(sftp.writeToFile(bundleFile),32*1024));
        sftp.chmod(bundleFile,0755);

        jdk.install(new RemoteLauncher(listener,connection),p,new SFTPFileSystem(sftp),listener, javaDir,bundleFile);
        return javaDir+"/bin/java";
    }

    /**
     * Starts the slave process.
     *
     * @param computer         The computer.
     * @param listener         The listener.
     * @param java             The full path name of the java executable to use.
     * @param workingDirectory The working directory from which to start the java process.
     *
     * @throws IOException If something goes wrong.
     */
    private void startSlave(SlaveComputer computer, final TaskListener listener, String java,
                            String workingDirectory) throws IOException {
        final Session session = connection.openSession();
        String cmd = "cd '" + workingDirectory + "' && " + java + (jvmOptions == null ? "" : " " + jvmOptions) + " -jar slave.jar";
        listener.getLogger().println(Messages.SSHLauncher_StartingSlaveProcess(getTimestamp(), cmd));
        session.execCommand(cmd);
        final StreamGobbler out = new StreamGobbler(session.getStdout());
        final StreamGobbler err = new StreamGobbler(session.getStderr());

        // capture error information from stderr. this will terminate itself
        // when the process is killed.
        new StreamCopyThread("stderr copier for remote agent on " + computer.getDisplayName(),
                err, listener.getLogger()).start();

        try {
            computer.setChannel(out, session.getStdin(), listener.getLogger(), new Channel.Listener() {
                @Override
                public void onClosed(Channel channel, IOException cause) {
                    if (cause != null) {
                        cause.printStackTrace(listener.error(hudson.model.Messages.Slave_Terminated(getTimestamp())));
                    }
                    try {
                        session.close();
                    } catch (Throwable t) {
                        t.printStackTrace(listener.error(Messages.SSHLauncher_ErrorWhileClosingConnection()));
                    }
                    try {
                        out.close();
                    } catch (Throwable t) {
                        t.printStackTrace(listener.error(Messages.SSHLauncher_ErrorWhileClosingConnection()));
                    }
                    try {
                        err.close();
                    } catch (Throwable t) {
                        t.printStackTrace(listener.error(Messages.SSHLauncher_ErrorWhileClosingConnection()));
                    }
                }
            });

        } catch (InterruptedException e) {
            session.close();
            throw new IOException2(Messages.SSHLauncher_AbortedDuringConnectionOpen(), e);
        }
    }

    /**
     * Method copies the slave jar to the remote system.
     *
     * @param listener         The listener.
     * @param workingDirectory The directory into whihc the slave jar will be copied.
     *
     * @throws IOException If something goes wrong.
     */
    private void copySlaveJar(TaskListener listener, String workingDirectory) throws IOException {
        String fileName = workingDirectory + "/slave.jar";

        listener.getLogger().println(Messages.SSHLauncher_StartingSFTPClient(getTimestamp()));
        SFTPClient sftpClient = null;
        try {
            sftpClient = new SFTPClient(connection);

            try {
                SFTPv3FileAttributes fileAttributes = sftpClient._stat(workingDirectory);
                if (fileAttributes==null) {
                    listener.getLogger().println(Messages.SSHLauncher_RemoteFSDoesNotExist(getTimestamp(),
                            workingDirectory));
                    sftpClient.mkdirs(workingDirectory, 0700);
                } else if (fileAttributes.isRegularFile()) {
                    throw new IOException(Messages.SSHLauncher_RemoteFSIsAFile(workingDirectory));
                }

                try {
                    // try to delete the file in case the slave we are copying is shorter than the slave
                    // that is already there
                    sftpClient.rm(fileName);
                } catch (IOException e) {
                    // the file did not exist... so no need to delete it!
                }

                listener.getLogger().println(Messages.SSHLauncher_CopyingSlaveJar(getTimestamp()));

                try {
                    CountingOutputStream os = new CountingOutputStream(sftpClient.writeToFile(fileName));
                    Util.copyStreamAndClose(
                            Hudson.getInstance().servletContext.getResourceAsStream("/WEB-INF/slave.jar"),
                            os);
                    listener.getLogger().println(Messages.SSHLauncher_CopiedXXXBytes(getTimestamp(), os.getByteCount()));
                } catch (Exception e) {
                    throw new IOException2(Messages.SSHLauncher_ErrorCopyingSlaveJar(), e);
                }
            } catch (Exception e) {
                throw new IOException2(Messages.SSHLauncher_ErrorCopyingSlaveJar(), e);
            }
        } finally {
            if (sftpClient != null) {
                sftpClient.close();
            }
        }
    }

    private void reportEnvironment(TaskListener listener) throws IOException, InterruptedException {
        listener.getLogger().println(Messages._SSHLauncher_RemoteUserEnvironment(getTimestamp()));
        connection.exec("set",listener.getLogger());
    }

    private String checkJavaVersion(TaskListener listener, String javaCommand) throws IOException, InterruptedException {
        listener.getLogger().println(Messages.SSHLauncher_CheckingDefaultJava(getTimestamp(),javaCommand));
        String line;
        StringWriter output = new StringWriter();   // record output from Java

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        connection.exec(javaCommand + " "+jvmOptions + " -version",out);
        BufferedReader r = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));
        while (null != (line = r.readLine())) {
            output.write(line);
            output.write("\n");
            line = line.toLowerCase();
            if (line.startsWith("java version \"") || line.startsWith("openjdk version \"")) {
                line = line.substring(line.indexOf('\"') + 1, line.lastIndexOf('\"'));
                listener.getLogger().println(Messages.SSHLauncher_JavaVersionResult(getTimestamp(), javaCommand, line));

                // TODO make this version check a bit less hacky
                if (line.compareTo("1.5") < 0) {
                    // TODO find a java that is at least 1.5
                    throw new IOException(Messages.SSHLauncher_NoJavaFound(line));
                }
                return javaCommand;
            }
        }

        listener.getLogger().println(Messages.SSHLauncher_UknownJavaVersion(javaCommand));
        listener.getLogger().println(output);
        throw new IOException(Messages.SSHLauncher_UknownJavaVersion(javaCommand));
    }

    private void openConnection(TaskListener listener) throws IOException {
        listener.getLogger().println(Messages.SSHLauncher_OpeningSSHConnection(getTimestamp(), host + ":" + port));
        connection.connect();
        
        String username = this.username;
        if(fixEmpty(username)==null) {
            username = System.getProperty("user.name");
            LOGGER.fine("Defaulting the user name to "+username);
        }

        boolean isAuthenticated = false;
        if(fixEmpty(privatekey)==null && fixEmpty(password)==null) {
            // check the default key locations if no authentication method is explicitly configured.
            File home = new File(System.getProperty("user.home"));
            for (String keyName : Arrays.asList("id_rsa","id_dsa","identity")) {
                File key = new File(home,".ssh/"+keyName);
                if (key.exists()) {
                    listener.getLogger()
                            .println(Messages.SSHLauncher_AuthenticatingPublicKey(getTimestamp(), username, key));
                    isAuthenticated = connection.authenticateWithPublicKey(username, key, null);
                }
                if (isAuthenticated)
                    break;
            }
        }
        if (!isAuthenticated && fixEmpty(privatekey)!=null) {
            File key = new File(privatekey);
            if (key.exists()) {
                listener.getLogger()
                        .println(Messages.SSHLauncher_AuthenticatingPublicKey(getTimestamp(), username, privatekey));
                if (PuTTYKey.isPuTTYKeyFile(key)) {
                    LOGGER.fine(key+" is a PuTTY key file");
                    String openSshKey = new PuTTYKey(key, password).toOpenSSH();
                    isAuthenticated = connection.authenticateWithPublicKey(username, openSshKey.toCharArray(), password);
                } else {
                    isAuthenticated = connection.authenticateWithPublicKey(username, key, password);
                }
            }
        }
        if (!isAuthenticated) {
            listener.getLogger()
                    .println(Messages.SSHLauncher_AuthenticatingUserPass(getTimestamp(), username, "******"));
            isAuthenticated = connection.authenticateWithPassword(username, password);
        }

        if (isAuthenticated && connection.isAuthenticationComplete()) {
            listener.getLogger().println(Messages.SSHLauncher_AuthenticationSuccessful(getTimestamp()));
        } else {
            listener.getLogger().println(Messages.SSHLauncher_AuthenticationFailed(getTimestamp()));
            connection.close();
            connection = null;
            listener.getLogger().println(Messages.SSHLauncher_ConnectionClosed(getTimestamp()));
            throw new AbortException(Messages.SSHLauncher_AuthenticationFailedException());
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void afterDisconnect(SlaveComputer slaveComputer, StreamTaskListener listener) {
        String workingDirectory = getWorkingDirectory(slaveComputer);
        String fileName = workingDirectory + "/slave.jar";

        if (connection != null) {

            SFTPv3Client sftpClient = null;
            try {
                sftpClient = new SFTPv3Client(connection);
                sftpClient.rm(fileName);
            } catch (Exception e) {
                e.printStackTrace(listener.error(Messages.SSHLauncher_ErrorDeletingFile(getTimestamp())));
            } finally {
                if (sftpClient != null) {
                    sftpClient.close();
                }
            }

            connection.close();
            PluginImpl.unregister(connection);
            connection = null;
            listener.getLogger().println(Messages.SSHLauncher_ConnectionClosed(getTimestamp()));
        }
        super.afterDisconnect(slaveComputer, listener);
    }

    /**
     * Getter for property 'host'.
     *
     * @return Value for property 'host'.
     */
    public String getHost() {
        return host;
    }

    /**
     * Getter for property 'port'.
     *
     * @return Value for property 'port'.
     */
    public int getPort() {
        return port;
    }

    /**
     * Getter for property 'username'.
     *
     * @return Value for property 'username'.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter for property 'password'.
     *
     * @return Value for property 'password'.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Getter for property 'privatekey'.
     *
     * @return Value for property 'privatekey'.
     */
    public String getPrivatekey() {
        return privatekey;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ComputerLauncher> {

        // TODO move the authentication storage to descriptor... see SubversionSCM.java

        // TODO add support for key files

        /**
         * {@inheritDoc}
         */
        public String getDisplayName() {
            return Messages.SSHLauncher_DescriptorDisplayName();
        }

    }

    @Extension
    public static class DefaultJavaProvider extends JavaProvider {
        public List<String> getJavas(SlaveComputer computer, TaskListener listener, Connection connection) {
            return Arrays.asList("java",
                    "/usr/bin/java",
                    "/usr/java/default/bin/java",
                    "/usr/java/latest/bin/java",
                    "/usr/local/bin/java",
                    "/usr/local/java/bin/java",
                    getWorkingDirectory(computer)+"/jdk/bin/java"); // this is where we attempt to auto-install
        }
    }

    private static final Logger LOGGER = Logger.getLogger(SSHLauncher.class.getName());

//    static {
//        com.trilead.ssh2.log.Logger.enabled = true;
//        com.trilead.ssh2.log.Logger.logger = new DebugLogger() {
//            public void log(int level, String className, String message) {
//                System.out.println(className+"\n"+message);
//            }
//        };
//    }
}
