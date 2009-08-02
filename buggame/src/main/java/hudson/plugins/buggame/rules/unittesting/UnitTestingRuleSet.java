package hudson.plugins.buggame.rules.unittesting;

import hudson.plugins.buggame.model.RuleSet;

/**
 * Rule set for unit test rules.
 */
public class UnitTestingRuleSet extends RuleSet {
    public UnitTestingRuleSet() {
        super("Unit testing");
        add(new IncreasingFailedTestsRule());
        add(new IncreasingPassedTestsRule());
    }
}
