package hudson.plugins.build_publisher;

import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.tasks.Mailer;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * And now something REALLY ugly. This is copy of Hudson's MailSender, with several changes needed for BuildPublisher.
 * I hope mail notification will become more modular soon.
 */

/**
 * Core logic of sending out notification e-mail.
 *
 * @author Jesse Glick
 * @author Kohsuke Kawaguchi
 */
public class MailSender2<P extends AbstractProject<P, B>, B extends AbstractBuild<P, B>> {
    /**
     * Whitespace-separated list of e-mail addresses that represent recipients.
     */
    private String recipients;

    /**
     * If true, only the first unstable build will be reported.
     */
    private boolean dontNotifyEveryUnstableBuild;

    /**
     * If true, individuals will receive e-mails regarding who broke the build.
     */
    private boolean sendToIndividuals;

    /**
     * If true, all builds will be reported
     */
    private boolean notifyEveryBuild;

    private String hudsonUrl;


    public MailSender2(String recipients, boolean dontNotifyEveryUnstableBuild, boolean sendToIndividuals, boolean notifyEveryBuild, String hudsonUrl) {
        this.recipients = recipients;
        this.dontNotifyEveryUnstableBuild = dontNotifyEveryUnstableBuild;
        this.sendToIndividuals = sendToIndividuals;
        this.notifyEveryBuild = notifyEveryBuild;
        this.hudsonUrl = hudsonUrl;
    }

    public boolean execute(B build, BuildListener listener) throws InterruptedException {
        try {
            MimeMessage mail = getMail(build, listener);
            if (mail != null) {
                Address[] allRecipients = mail.getAllRecipients();
                if (allRecipients != null) {
                    StringBuffer buf = new StringBuffer("Sending e-mails to:");
                    for (Address a : allRecipients)
                        buf.append(' ').append(a);
                    listener.getLogger().println(buf);
                    Transport.send(mail);
                } else {
                    listener.getLogger().println("An attempt to send an e-mail"
                        + " to empty list of recipients, ignored.");
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace(listener.error(e.getMessage()));
        }

        return true;
    }

    protected MimeMessage getMail(B build, BuildListener listener) throws MessagingException, InterruptedException {
        if (build.getResult() == Result.FAILURE) {
            return createFailureMail(build, listener);
        }

        if (build.getResult() == Result.UNSTABLE) {
            B prev = build.getPreviousBuild();
            if (notifyEveryBuild || !dontNotifyEveryUnstableBuild)
                return createUnstableMail(build, listener);
            if (prev != null) {
                if (prev.getResult() == Result.SUCCESS)
                    return createUnstableMail(build, listener);
            }
        }

        if (build.getResult() == Result.SUCCESS) {
            B prev = build.getPreviousBuild();
                        if (notifyEveryBuild)
                                return createNormalMail(build, "Hudson build completed",
                                        listener);
                            if ((prev != null) || notifyEveryBuild) {
                if (prev.getResult() == Result.FAILURE)
                    return createNormalMail(build,"Hudson build is back to normal", listener);
                if (prev.getResult() == Result.UNSTABLE)
                    return createNormalMail(build, "Hudson build is back to stable", listener);
            }
        }

        return null;
    }

    private MimeMessage createNormalMail(B build, String subject, BuildListener listener) throws MessagingException {
        MimeMessage msg = createEmptyMail(build, listener);

        msg.setSubject(getSubject(build, subject + ": "), "UTF-8");
        StringBuffer buf = new StringBuffer();
        appendBuildUrl(build, buf);
        msg.setText(buf.toString());

        return msg;
    }

    private MimeMessage createUnstableMail(B build, BuildListener listener) throws MessagingException {
        MimeMessage msg = createEmptyMail(build, listener);

        msg.setSubject(getSubject(build, "Hudson build became unstable: "),"UTF-8");
        StringBuffer buf = new StringBuffer();
        appendBuildUrl(build, buf);
        msg.setText(buf.toString());

        return msg;
    }

    private void appendBuildUrl(B build, StringBuffer buf) {
        String baseUrl = hudsonUrl;
        if (baseUrl != null) {
            buf.append("See ").append(baseUrl).append(Util.encode(build.getUrl())).append("changes\n\n");
        }
    }

    private MimeMessage createFailureMail(B build, BuildListener listener) throws MessagingException, InterruptedException {
        MimeMessage msg = createEmptyMail(build, listener);

        msg.setSubject(getSubject(build, "Build failed in Hudson: "),"UTF-8");

        StringBuffer buf = new StringBuffer();
        appendBuildUrl(build, buf);

        boolean firstChange = true;
        for (ChangeLogSet.Entry entry : build.getChangeSet()) {
            if (firstChange) {
                firstChange = false;
                buf.append("Changes:\n\n");
            }
            buf.append('[');
            buf.append(entry.getAuthor().getFullName());
            buf.append("] ");
            String m = entry.getMsg();
            buf.append(m);
            if (!m.endsWith("\n")) {
                buf.append('\n');
            }
            buf.append('\n');
        }

        buf.append("------------------------------------------\n");

        try {
            String log = build.getLog();
            String[] lines = log.split("\n");
            int start = 0;
            if (lines.length > MAX_LOG_LINES) {
                // Avoid sending enormous logs over email.
                // Interested users can always look at the log on the web server.
                buf.append("[...truncated " + (lines.length - MAX_LOG_LINES) + " lines...]\n");
                start = lines.length - MAX_LOG_LINES;
            }
            String workspaceUrl = null, artifactUrl = null;
            Pattern wsPattern = null;
            String baseUrl = hudsonUrl;
            if (baseUrl != null) {
                // Hyperlink local file paths to the repository workspace or build artifacts.
                // Note that it is possible for a failure mail to refer to a file using a workspace
                // URL which has already been corrected in a subsequent build. To fix, archive.
                workspaceUrl = baseUrl + Util.encode(build.getProject().getUrl()) + "ws/";
                artifactUrl = baseUrl + Util.encode(build.getUrl()) + "artifact/";
                FilePath ws = build.getProject().getWorkspace();
                // Match either file or URL patterns, i.e. either
                // c:\hudson\workdir\jobs\foo\workspace\src\Foo.java
                // file:/c:/hudson/workdir/jobs/foo/workspace/src/Foo.java
                // will be mapped to one of:
                // http://host/hudson/job/foo/ws/src/Foo.java
                // http://host/hudson/job/foo/123/artifact/src/Foo.java
                // Careful with path separator between $1 and $2:
                // workspaceDir will not normally end with one;
                // workspaceDir.toURI() will end with '/' if and only if workspaceDir.exists() at time of call
                wsPattern = Pattern.compile("(" +
                    quoteRegexp(ws.getRemote()) + "|" + quoteRegexp(ws.toURI().toString()) + ")[/\\\\]?([^:#\\s]*)");
            }
            for (int i = start; i < lines.length; i++) {
                String line = lines[i];
                if (wsPattern != null) {
                    // Perl: $line =~ s{$rx}{$path = $2; $path =~ s!\\\\!/!g; $workspaceUrl . $path}eg;
                    Matcher m = wsPattern.matcher(line);
                    int pos = 0;
                    while (m.find(pos)) {
                        String path = m.group(2).replace(File.separatorChar, '/');
                        String linkUrl = artifactMatches(path, build) ? artifactUrl : workspaceUrl;
                        // Append ' ' to make sure mail readers do not interpret following ':' as part of URL:
                        String prefix = line.substring(0, m.start()) + linkUrl + Util.encode(path) + ' ';
                        pos = prefix.length();
                        line = prefix + line.substring(m.end());
                        // XXX better style to reuse Matcher and fix offsets, but more work
                        m = wsPattern.matcher(line);
                    }
                }
                buf.append(line);
                buf.append('\n');
            }
        } catch (IOException e) {
            // somehow failed to read the contents of the log
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            buf.append("Failed to access build log\n\n").append(sw);
        }

        msg.setText(buf.toString());

        return msg;
    }

    private MimeMessage createEmptyMail(B build, BuildListener listener) throws MessagingException {
        MimeMessage msg = new MimeMessage(Mailer.DESCRIPTOR.createSession());
        // TODO: I'd like to put the URL to the page in here,
        // but how do I obtain that?
        msg.setContent("", "text/plain");
        msg.setFrom(new InternetAddress(Mailer.DESCRIPTOR.getAdminAddress()));
        msg.setSentDate(new Date());

        Set<InternetAddress> rcp = new LinkedHashSet<InternetAddress>();
        StringTokenizer tokens = new StringTokenizer(recipients);
        while (tokens.hasMoreTokens())
            rcp.add(new InternetAddress(tokens.nextToken()));
        if (sendToIndividuals) {
            Set<User> culprits = build.getCulprits();

            if(debug)
                listener.getLogger().println("Trying to send e-mails to individuals who broke the build. sizeof(culprits)=="+culprits.size());

            for (User a : culprits) {
                String adrs = Util.fixEmpty(a.getProperty(Mailer.UserProperty.class).getAddress());
                if(debug)
                    listener.getLogger().println("  User "+a.getId()+" -> "+adrs);
                if (adrs != null)
                    rcp.add(new InternetAddress(adrs));
                else {
                    listener.getLogger().println("Failed to send e-mail to " + a.getFullName() + " because no e-mail address is known, and no default e-mail domain is configured");
                }
            }
        }
        msg.setRecipients(Message.RecipientType.TO, rcp.toArray(new InternetAddress[rcp.size()]));
        return msg;
    }

    private String getSubject(B build, String caption) {
        return caption + build.getProject().getFullDisplayName() + " #" + build.getNumber();
    }

    /**
     * Copied from JDK5, to avoid 5.0 dependency.
     */
    private static String quoteRegexp(String s) {
        int slashEIndex = s.indexOf("\\E");
        if (slashEIndex == -1)
            return "\\Q" + s + "\\E";

        StringBuilder sb = new StringBuilder(s.length() * 2);
        sb.append("\\Q");
        int current = 0;
        while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
            sb.append(s.substring(current, slashEIndex));
            current = slashEIndex + 2;
            sb.append("\\E\\\\E\\Q");
        }
        sb.append(s.substring(current, s.length()));
        sb.append("\\E");
        return sb.toString();
    }

    /**
     * Check whether a path (/-separated) will be archived.
     */
    protected boolean artifactMatches(String path, B build) {
        return false;
    }


    private static final Logger LOGGER = Logger.getLogger(MailSender2.class.getName());

    public static boolean debug = false;

    private static final int MAX_LOG_LINES = 250;

}
