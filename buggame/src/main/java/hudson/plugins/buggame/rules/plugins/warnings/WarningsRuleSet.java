package hudson.plugins.buggame.rules.plugins.warnings;

import hudson.plugins.buggame.rules.plugins.PluginRuleSet;

public class WarningsRuleSet extends PluginRuleSet {

    public WarningsRuleSet() {
        super("warnings", "Compiler warnings");
    } 

    @Override
    protected void loadRules() {
        add(new DefaultWarningsRule(-1, 1));
    }
}
