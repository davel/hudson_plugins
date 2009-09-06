package org.jggug.hudson.plugins.gcrawler;

import static java.lang.String.format;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

public class CrawlLogger {

    private static final String LOG_FORMAT = "[%1$s] %2$tT.%2$tL %3$s%n";

    private static final Logger logger = Logger.getLogger(CrawlLogger.class.getName());

    private Writer writer;

    public CrawlLogger() {
    }

    public CrawlLogger(File logFile) {
        try {
            logFile.deleteOnExit();
            writer = new BufferedWriter(new FileWriter(logFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        info(format("Log to %s.", logFile));
    }

    public void warn(Throwable e) {
        log(WARNING, ExceptionUtils.getFullStackTrace(e));
    }

    public void warn(String message) {
        log(WARNING, message);
    }

    public void info(String message) {
        log(INFO, message);
    }

    public void debug(String message) {
        log(FINE, message);
    }

    private synchronized void log(Level level, String message) {
        if (!logger.isLoggable(level)) {
            return;
        }
        logger.log(level, message);
        if (writer != null) {
            try {
                writer.append(format(LOG_FORMAT, level, new Date(), message));
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void close() {
        if (writer == null) {
            return;
        }
        try {
            writer.flush();
        } catch (IOException ignore) {
            // NOP
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }
}
