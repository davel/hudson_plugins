package hudson.plugins.jswidgets;

import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.RootAction;
import hudson.util.RunList;

import org.kohsuke.stapler.StaplerRequest;

/**
 * Implements methods for javascript root widgets.
 * 
 * @author mfriedenhagen
 */
@Extension
public class JsRootAction extends JsBaseAction implements RootAction {

    /** 
     * {@inheritDoc}
     *
     * This actions always starts from the context directly, so prefix {@link JsConsts} with a slash. 
     */
    @Override
    public String getUrlName() {
        return "/" + JsConsts.URLNAME;
    }
    
    /**
     * Returns some or all known runs of this hudson instance, depending on parameter count.
     * 
     * @param request evalutes parameter <tt>count</tt>
     * @return runlist
     */
    public RunList getRunList(StaplerRequest request) {
        final RunList allRuns = new RunList(Hudson.getInstance().getPrimaryView());
        final String countParameter = request.getParameter("count");
        if (countParameter == null) {
            return allRuns;
        } else {            
            final int count = Integer.valueOf(countParameter);
            if (count > allRuns.size()) {
                return allRuns;
            } else {
                final RunList runList = new RunList();
                for (int i = 0; i < count; i++) {
                    runList.add(allRuns.get(i));
                }
                return runList;
            }
        }
    }

}
