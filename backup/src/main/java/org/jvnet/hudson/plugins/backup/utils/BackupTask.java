package org.jvnet.hudson.plugins.backup.utils;

import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.security.ACL;

import java.io.FileFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.jvnet.hudson.plugins.backup.BackupConfig;
import org.jvnet.hudson.plugins.backup.BackupException;

/**
 * This is the backup task, run in background and log to a file
 *
 * @author vsellier
 */
public class BackupTask extends BackupPluginTask {
	private final static String WORKSPACE_NAME = "workspace";
	private final static String FINGERPRINTS_NAME = "fingerprints";
	private final static String BUILDS_NAME = "**/builds";
	private final static String ARCHIVE_NAME = "**/archive";	
	private final static String[] DEFAULT_EXCLUSIONS = { "backup.log" };

	
	/**
     * delay between two verification of no running processes
     */
    private final static Integer DELAY = 5000;


    public BackupTask(BackupConfig configuration, String hudsonWorkDir, String backupFileName, String logFilePath) {
        super(configuration, hudsonWorkDir, backupFileName, logFilePath);
    }

    public void run() {
        assert (logFilePath != null);
        assert (configuration.getFileNameTemplate() != null);

        SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);
        
        startDate = new Date();
        try {
            logger = new BackupLogger(logFilePath, configuration.isVerbose());
        } catch (IOException e) {
            LOGGER.severe("Unable to open log file for writing : "
                    + logFilePath);
            return;
        }

        logger.info("Backup started at " + getTimestamp(startDate));

        // Have to include shutdown time in backup time ?
        waitNoJobsInQueue();

        // Creating exclusions
        List<String> exclusions = new ArrayList<String>();
    	exclusions.addAll(Arrays.asList(DEFAULT_EXCLUSIONS));
    	exclusions.addAll(configuration.getCustomExclusions());
    	if (! configuration.getKeepWorkspaces()) {
    		exclusions.add(WORKSPACE_NAME);
    	}
    	if (! configuration.getKeepFingerprints()) {
    		exclusions.add(FINGERPRINTS_NAME);
    	}
    	if (! configuration.getKeepBuilds()) {
    		exclusions.add(BUILDS_NAME);
    	}
    	if (! configuration.getKeepArchives()) {
    		exclusions.add(ARCHIVE_NAME);
    	}
        
        FileFilter filter = createFileFilter(exclusions);

        try {
            BackupEngine backupEngine = new BackupEngine(logger,
                    hudsonWorkDir, backupFileName, configuration.getArchiveType().getArchiver(), filter);
            backupEngine.doBackup();
        } catch (BackupException e) {
            e.printStackTrace(logger.getWriter());
        } finally {
            cancelNoJobs();

            endDate = new Date();
            logger.info("Backup end at " + getTimestamp(endDate));
            BigDecimal delay = new BigDecimal(endDate.getTime()
                    - startDate.getTime());
            delay = delay.setScale(2, BigDecimal.ROUND_HALF_UP);
            delay = delay.divide(new BigDecimal("1000"));

            logger.info("[" + delay.toPlainString() + "s]");

            finished = true;
            logger.close();
        }
    }


    private FileFilter createFileFilter(List<String> exclusions) {
        // creating the filter
        IOFileFilter filter = new NameFileFilter(exclusions
                .toArray(new String[]{}));
        // revert it because they are exclusions
        filter = new NotFileFilter(filter);

        return filter;
    }

    private void waitNoJobsInQueue() {
        logger
                .info("Setting hudson in shutdown mode to avoid files corruptions.");
        try {
            Hudson.getInstance().doQuietDown();
        } catch (Exception e) {
            logger.error("Erreur putting hudson in shutdown mode.");
            e.printStackTrace(logger.getWriter());
        }

        logger.info("Waiting all jobs end...");

        Computer computer[] = Hudson.getInstance().getComputers();

        int running = 1;
        // TODO BACKUP-PLUGIN add timeout here
        while (running > 0) {
            running = 0;
            for (int i = 0; i < computer.length; i++) {
                running += computer[i].countBusy();
            }
            logger.info("Number of running jobs detected : " + running);

            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
            }
        }

        logger.info("All jobs finished.");
    }

    private void cancelNoJobs() {
        logger.info("Cancel hudson shutdown mode");
        try {
            Hudson.getInstance().doCancelQuietDown();
        } catch (Exception e) {
            logger.error("Erreur cancelling hudson shutdown mode.");
            e.printStackTrace(logger.getWriter());
        }
    }

}
