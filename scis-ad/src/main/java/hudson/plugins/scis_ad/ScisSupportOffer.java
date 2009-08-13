package hudson.plugins.scis_ad;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.AdministrativeMonitor;
import hudson.model.Hudson;
import hudson.util.TimeUnit2;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.File;
import java.io.IOException;

/**
 * Shows a support offer.
 *
 * To minimize the negative impact of such ads, we only show it after
 * (1) the user has been running Hudson for 2 months, (2) there seems to be reasonable number of jobs
 * that indicates an active use of Hudson.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class ScisSupportOffer extends AdministrativeMonitor {
    public volatile boolean active;
    public ScisSupportOffer() {
        Hudson h = Hudson.getInstance();
        File marker1 = new File(h.getRootDir(),"secret.key");
        File marker2 = new File(h.getRootDir(),"scis-reminder");

        long t = Math.max(marker1.lastModified(), marker2.lastModified());
        if (t>0) {
            long d = TimeUnit2.MILLISECONDS.toDays(System.currentTimeMillis() - t);
            active = d > 60 && h.getItems().size()>20;
        }
    }

    public boolean isActivated() {
        return active;
    }

    /**
     * Depending on whether the user said "yes" or "no", send him to the right place.
     */
    public void doAct(StaplerRequest req, StaplerResponse rsp, @QueryParameter String no) throws IOException, InterruptedException {
        if(no!=null) {
            disable(true);
        } else {
            // notify later
            active = false;
            new FilePath(Hudson.getInstance().getRootDir()).child("scis-reminder").touch(System.currentTimeMillis());
        }
        rsp.sendRedirect(req.getContextPath()+"/manage");
    }
}
