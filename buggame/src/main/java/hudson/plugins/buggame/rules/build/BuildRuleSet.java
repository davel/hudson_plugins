package hudson.plugins.buggame.rules.build;

import hudson.plugins.buggame.model.RuleSet;

/**
 * Rule set for common build rules.
 */
public class BuildRuleSet extends RuleSet {
    public BuildRuleSet() {
        super("Build result");
        add(new BuildResultRule());
    }
}
