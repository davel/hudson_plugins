package hudson.plugins.clearcase.action;

import hudson.FilePath;
import hudson.Launcher;
import hudson.plugins.clearcase.ClearTool;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Check out action that will check out files into a UCM snapshot view. Checking
 * out the files will also update the load rules in the view.
 */
public class UcmSnapshotCheckoutAction implements CheckOutAction {

    private ClearTool cleartool;
    
    private String stream;

    private String loadRules;

    public UcmSnapshotCheckoutAction(ClearTool cleartool, String stream,
            String loadRules) {
        super();
        this.cleartool = cleartool;
        this.stream = stream;
        this.loadRules = loadRules;
    }

    public boolean checkout(Launcher launcher, FilePath workspace, String viewName) 
    		throws IOException, InterruptedException {
        boolean localViewPathExists = new FilePath(workspace, viewName)
                .exists();

        if (localViewPathExists) {
            String configSpec = cleartool.catcs(viewName);
            Set<String> configSpecLoadRules = extractLoadRules(configSpec);
            boolean recreate = currentConfigSpecUptodate(configSpecLoadRules);
            if (recreate) {
                cleartool.rmview(viewName);
                cleartool.mkview(viewName, stream);
            }
        } else {
            cleartool.mkview(viewName, stream);
        }
        for (String loadRule : loadRules.split("\n")) {
            cleartool.update(viewName, loadRule.trim());
        }
        return true;
    }

    private boolean currentConfigSpecUptodate(Set<String> configSpecLoadRules) {
        boolean recreate = false;
        for (String loadRule : loadRules.split("\n")) {
            if (!configSpecLoadRules.contains(loadRule)) {
                System.out
                        .println("Load rule: "
                                + loadRule
                                + " not found in current config spec, forcing recreation of view");
                recreate = true;
            }
        }
        return recreate;
    }

    private Set<String> extractLoadRules(String configSpec) {
        Set<String> rules = new HashSet<String>();
        for (String row : configSpec.split("\n")) {
            String trimmedRow = row.toLowerCase().trim();
            if (trimmedRow.startsWith("load")) {
                String rule = row.trim().substring("load".length()).trim();
                rules.add(rule);
                if (!rule.startsWith("/")) {
                    rules.add("/" + rule);
                } else {
                    rules.add(rule.substring(1));
                }
            }
        }
        return rules;
    }

}
