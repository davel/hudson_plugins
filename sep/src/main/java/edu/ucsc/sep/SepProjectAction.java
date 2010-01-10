package edu.ucsc.sep;

import hudson.model.Action;

/**
 * User: cflewis
 * Date: Jan 9, 2010
 * Time: 5:01:09 PM
 */
public class SepProjectAction implements Action {
    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return "SEP";
    }

    public String getUrlName() {
        return "sep";
    }
}
