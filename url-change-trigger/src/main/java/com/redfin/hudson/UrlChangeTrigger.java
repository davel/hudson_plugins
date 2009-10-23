package com.redfin.hudson;

import hudson.Extension;
import static hudson.Util.*;
import hudson.model.BuildableItem;
import hudson.model.Item;
import hudson.scheduler.CronTabList;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.FormValidation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import antlr.ANTLRException;

/** Triggers a build when the data at a particular URL has changed. */
public class UrlChangeTrigger extends Trigger<BuildableItem> {

    URL url;
    
    private static final int readBufferSize = 8*1024;
    /** Extracts the MD5 string from the specified input stream; this MD5 should
     * be a perfect match for the GNU "md5sum" tool.
     */
    private static String getMd5(InputStream stream) throws IOException {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        DigestInputStream dis = new DigestInputStream(stream,
                messageDigest);
        byte[] buf = new byte[readBufferSize];
        while (dis.read(buf, 0, readBufferSize) != -1) { }
        dis.close();
        stream.close();
        byte[] fileDigest = messageDigest.digest ();
        return createDigestString(fileDigest);
    }
    
    /** byte array to string */
    private static String createDigestString(byte[] fileDigest) {
        StringBuffer checksumSb = new StringBuffer();
        for (int i = 0; i < fileDigest.length; i++) {
            String hexStr = Integer.toHexString(0x00ff & fileDigest[i]);
            if (hexStr.length() < 2) {
                checksumSb.append("0");
            }
            checksumSb.append(hexStr);
        }
        return checksumSb.toString();
    }

    public UrlChangeTrigger(String url) throws MalformedURLException {
        this(new URL(url));
    }
    
    public UrlChangeTrigger(URL url) {
        this.url = url;
    }
    
    @Override
    public void start(BuildableItem project, boolean newInstance) {
    	super.start(project, newInstance);
        try {
            this.tabs = CronTabList.create("* * * * *");
        } catch (ANTLRException e) {
            throw new RuntimeException("Bug! couldn't schedule poll");
        }   	
    }

    private static final Logger LOGGER =
        Logger.getLogger(UrlChangeTrigger.class.getName());

    private File getFingerprintFile() {
	return new File(job.getRootDir(), "url-change-trigger-oldmd5");
    }

    @Override
    public void run() {
        try {
            LOGGER.log(Level.FINER, "Testing the file {0}", url);
            String currentMd5 = getMd5(url.openStream());

	    String oldMd5;
	    File file = getFingerprintFile();
	    if (!file.exists()) {
		oldMd5 = "null";
	    } else {
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
		    oldMd5 = br.readLine();
		} finally {
		    br.close();
		}
	    }
            if (!currentMd5.equals(oldMd5)) {
            	LOGGER.log(Level.FINE, 
            			"Differences found in the file {0}. >{1}< != >{2}<",
            			new Object[] {
                                    url, oldMd5, currentMd5,
            			});
		PrintWriter w = new PrintWriter(new FileOutputStream(file));
		try {
		    w.println(currentMd5);
		} finally {
		    w.close();
		}
                oldMd5 = currentMd5;
                job.scheduleBuild(new UrlChangeCause(url));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public URL getUrl() {
        return url;
    }

    @Extension
    public static final class DescriptorImpl extends TriggerDescriptor {

        public DescriptorImpl() {
            super(UrlChangeTrigger.class);
        }

        @Override
        public boolean isApplicable(Item item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Build when a URL's content changes";
        }
        
        @Override
        public String getHelpFile() {
            return "/plugin/url-change-trigger/help-whatIsUrlChangeTrigger.html";
        }
        
        /**
         * Performs syntax check.
         */
        public FormValidation doCheck(@QueryParameter("urlChangeTrigger.url") String url) {
            try {
                new URL(fixNull(url));
                return FormValidation.ok();
            } catch (MalformedURLException e) {
                return FormValidation.error(e.getMessage());
            }
        }
        
        @Override
        public UrlChangeTrigger newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            String url = formData.getString("url");
            try {
                return new UrlChangeTrigger(url);
            } catch (MalformedURLException e) {
                throw new FormException("Invalid URL: " + url, e, "");
            }
        }
        
    }
}
