package edu.ucsc.soe.sep;

import hudson.model.Action;

/**
 * User: cflewis
 * Date: Jan 9, 2010
 * Time: 5:01:09 PM
 */
public class SepProjectAction implements Action {
    public String getIconFileName() {
        return "BLAH.png";
    }

    public String getDisplayName() {
        return "Sep";
    }

    public String getUrlName() {
        return "sep";
    }
}
