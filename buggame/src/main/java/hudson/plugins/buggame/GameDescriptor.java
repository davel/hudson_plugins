package hudson.plugins.buggame;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.plugins.buggame.model.RuleBook;
import hudson.plugins.buggame.model.RuleSet;
import hudson.plugins.buggame.rules.build.BuildRuleSet;
import hudson.plugins.buggame.rules.plugins.checkstyle.CheckstyleRuleSet;
import hudson.plugins.buggame.rules.plugins.findbugs.FindBugsRuleSet;
import hudson.plugins.buggame.rules.plugins.opentasks.OpenTasksRuleSet;
import hudson.plugins.buggame.rules.plugins.pmd.PmdRuleSet;
import hudson.plugins.buggame.rules.plugins.violation.ViolationsRuleSet;
import hudson.plugins.buggame.rules.plugins.warnings.WarningsRuleSet;
import hudson.plugins.buggame.rules.unittesting.UnitTestingRuleSet;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;

@Extension
public class GameDescriptor extends BuildStepDescriptor<Publisher> {

    public static final String ACTION_LOGO_LARGE = "star-large-gold.gif";
    public static final String ACTION_LOGO_MEDIUM = "star-gold.gif";

    private RuleBook rulebook;

    public GameDescriptor() {
        super(GamePublisher.class);
    }

    /**
     * Returns the default rule book
     * 
     * @return the rule book that is configured for the game.
     */
    public RuleBook getRuleBook() {
        if (rulebook == null) {
            rulebook = new RuleBook();

            addRuleSetIfAvailable(rulebook, new BuildRuleSet());
            addRuleSetIfAvailable(rulebook, new UnitTestingRuleSet());
            addRuleSetIfAvailable(rulebook, new OpenTasksRuleSet());
            addRuleSetIfAvailable(rulebook, new ViolationsRuleSet());
            addRuleSetIfAvailable(rulebook, new PmdRuleSet());
            addRuleSetIfAvailable(rulebook, new FindBugsRuleSet());
            addRuleSetIfAvailable(rulebook, new WarningsRuleSet());
            addRuleSetIfAvailable(rulebook, new CheckstyleRuleSet());
        }
        return rulebook;
    }

    private void addRuleSetIfAvailable(RuleBook book, RuleSet ruleSet) {
        if (ruleSet.isAvailable()) {
            book.addRuleSet(ruleSet);
        }
    }

    @Override
    public String getDisplayName() {
        return "Continuous Integration Game";
    }

    @Override
    public Publisher newInstance(StaplerRequest req, JSONObject formData)
            throws hudson.model.Descriptor.FormException {
        return new GamePublisher();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
    	return true;
    }
}
