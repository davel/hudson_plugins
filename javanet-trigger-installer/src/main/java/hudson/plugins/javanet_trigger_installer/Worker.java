package hudson.plugins.javanet_trigger_installer;

import org.kohsuke.jnt.JavaNet;
import org.kohsuke.jnt.ProcessingException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.util.TimeUnit2;

/**
 * Does the actual job of subscribing/unsubscribing.
 *
 * <p>
 * The programmatic access to java.net often takes a long time
 * to run, so the actual work is done in a separate thread
 * asynchronously.
 *
 * @author Kohsuke Kawaguchi
 */
public class Worker extends Thread {
    /**
     * Job queue.
     */
    /*package*/ static final List<Task> queue = new LinkedList<Task>();

    public Worker() {
        super("java.net SCM trigger worker thread");
    }

    public void run() {
        long lastUsed;
        try {
            JavaNet connection=null;
            while(true) {
                try {
                    lastUsed = System.currentTimeMillis();

                    Task t = pick();

                    // if a connection is left unused for a long time,
                    // don't bother using it. it must have already gone expired.
                    if(System.currentTimeMillis()-lastUsed >= SESSION_TIMEOUT)
                        connection = null;

                    Exception ex = null;
                    for( int i=0; i<2; i++ ) {// allow one retry, in case the connection becomes expired

                        if(connection==null)
                            connection = JavaNet.connect();

                        try {
                            t.execute(connection);
                            break;

                            // in case of error, force a fresh connection and retry
                        } catch (IOException e) {
                            connection = null;
                            ex = e;
                        } catch (ProcessingException e) {
                            connection = null;
                            ex = e;
                        }
                    }

                    if(ex!=null)
                        LOGGER.log(Level.SEVERE,"Failed to execute "+t,ex);
                } catch (InterruptedException e) {
                    throw e;
                } catch (Throwable t) {
                    LOGGER.log(Level.SEVERE,"Terminated abnormally because of an error. Retrying after a minute",t);
                    Thread.sleep(60*1000);
                }
            }
        } catch (InterruptedException e) {
            LOGGER.info("Going to shut down");
        }
    }

    /**
     * Pick up the next task
     */
    private Task pick() throws InterruptedException {
        synchronized(queue) {
            while(queue.isEmpty())
                queue.wait();
            return queue.remove(0);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(Worker.class.getName());

    private static final long SESSION_TIMEOUT = TimeUnit2.MINUTES.toMillis(120);
}
