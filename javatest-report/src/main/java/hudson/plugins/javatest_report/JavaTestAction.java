/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package hudson.plugins.javatest_report;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.test.AbstractTestResultAction;
import org.kohsuke.stapler.StaplerProxy;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Action} that displays the JavaTest test result.
 *
 * <p>
 * The actual test reports are isolated by {@link java.lang.ref.WeakReference}
 * so that it doesn't eat up too much memory.
 *
 * @author Kohsuke Kawaguchi
 */
public class JavaTestAction extends AbstractTestResultAction<JavaTestAction> implements StaplerProxy {
    private transient WeakReference<Report> result;

    private int failCount;
    private int totalCount;

    JavaTestAction(AbstractBuild<?,?> owner, BuildListener listener) {
        super(owner);

        Report r = load(listener);
        totalCount = r.getTotalCount();
        failCount = r.getFailCount();

        result = new WeakReference<Report>(r);
    }

    /*package*/ static File getDataDir(AbstractBuild<?,?> build) {
        return new File(build.getRootDir(), "java-test-result");
    }

    public synchronized Report getResult() {
        if(result==null) {
            Report r = load(null);
            result = new WeakReference<Report>(r);
            return r;
        }
        Report r = result.get();
        if(r==null) {
            r = load(null);
            result = new WeakReference<Report>(r);
        }
        return r;
    }

    /**
     * Gets the number of failed tests.
     */
    public int getFailCount() {
        return failCount;
    }

    /**
     * Gets the total number of tests.
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Loads a {@link hudson.tasks.junit.TestResult} from disk.
     *
     * @param listener
     *      Can be null. If available, error reports should be sent there.
     */
    private Report load(BuildListener listener) {
        Report r = new Report(this);
        File[] files = getDataDir(owner).listFiles();
        if(files==null) {
            JavaTestAction.logger.log(Level.WARNING, "No test reports found in "+getDataDir(owner));
            return r;
        }

        for (File f : files) {
            try {
                if(f.isFile() && f.getName().endsWith(".xml"))
                    r.add(f);
            } catch (IOException e) {
                if(listener!=null)
                    e.printStackTrace(listener.error("Failed to parse "+f));
                else
                    JavaTestAction.logger.log(Level.WARNING, "Failed to load "+f,e);
            } catch (SAXException e) {
                if(listener!=null)
                    e.printStackTrace(listener.error("Failed to parse "+f));
                else
                    JavaTestAction.logger.log(Level.WARNING, "Failed to load "+f,e);
            }
        }

        return r;
    }

    public Object getTarget() {
        return getResult();
    }

    private static final Logger logger = Logger.getLogger(JavaTestAction.class.getName());
}
