package hudson.plugins.cigame.rules.plugins.violation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import hudson.model.AbstractBuild;
import hudson.model.Build;
import hudson.model.Result;
import hudson.plugins.cigame.model.RuleResult;
import hudson.plugins.violations.ViolationsBuildAction;
import hudson.plugins.violations.ViolationsReport;
import hudson.plugins.violations.ViolationsReport.TypeReport;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class DefaultViolationRuleTest {
    
    private Mockery context;
    private Mockery classContext;
    private AbstractBuild<?,?> build;
    private AbstractBuild<?,?> previousBuild;
    
    @Before
    public void setUp() throws Exception {
        context = new Mockery();
        classContext = new Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
        build = classContext.mock(AbstractBuild.class);
        previousBuild = classContext.mock(AbstractBuild.class);        
    }
    
    @Test
    public void assertFailedBuildsIsWorthZeroPoints() {
        
        final Result buildResult = Result.FAILURE;
        classContext.checking(new Expectations() {
            {
                ignoring(build).getResult(); will(returnValue(buildResult));
            }
        });

        DefaultViolationRule rule = new DefaultViolationRule("pmd", "PMD Violations", 100, -100);
        RuleResult ruleResult = rule.evaluate(build);
        assertNotNull("Rule result must not be null", ruleResult);
        assertThat("Points should be zero", ruleResult.getPoints(), is((double) 0));
        
        classContext.assertIsSatisfied();
        context.assertIsSatisfied();
    }

    /**
     * Asserts that the issue 1884 is fixed.
     * https://hudson.dev.java.net/issues/show_bug.cgi?id=1884
     */
    @Test
    public void assertIssue1884IsFixed() {
        final ViolationsReport previousReport = createViolationsReportStub("pmd", 200, null);
        final ViolationsReport currentReport = createViolationsReportStub("pmd", 100, previousReport);
        final ArrayList<ViolationsBuildAction> actionList = new ArrayList<ViolationsBuildAction>();
        actionList.add(new ViolationsBuildAction(null, currentReport));
        
        final Result buildResult = Result.FAILURE;
        classContext.checking(new Expectations() {
            {
                ignoring(build).getResult(); will(returnValue(buildResult));
                ignoring(build).getPreviousBuild(); will(returnValue(previousBuild));
                ignoring(build).getActions(ViolationsBuildAction.class); will(returnValue(actionList));
            }
        });

        DefaultViolationRule rule = new DefaultViolationRule("pmd", "PMD Violations", 100, -100);
        RuleResult ruleResult = rule.evaluate(build);
        assertNotNull("Rule result must not be null", ruleResult);
        assertThat("Points should be zero", ruleResult.getPoints(), is((double) 0));
        
        classContext.assertIsSatisfied();
        context.assertIsSatisfied();
    }
    
    @Test
    public void assertNoPreviousBuildIsWorthZeroPoints() {        
        classContext.checking(new Expectations() {
            {
                ignoring(build).getResult(); will(returnValue(Result.SUCCESS));
                ignoring(build).getPreviousBuild(); will(returnValue(null));
            }
        });

        DefaultViolationRule rule = new DefaultViolationRule("pmd", "PMD Violations", 100, -100);
        RuleResult ruleResult = rule.evaluate(build);
        assertNotNull("Rule result must not be null", ruleResult);
        assertThat("Points should be zero", ruleResult.getPoints(), is((double) 0));
        
        classContext.assertIsSatisfied();
        context.assertIsSatisfied();
    }
    
    @Test
    public void assertIfPreviousBuildFailedResultIsWorthZeroPoints() {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractBuild previousBuild = mock(AbstractBuild.class);
        when(build.getPreviousBuild()).thenReturn(previousBuild);
        when(build.getResult()).thenReturn(Result.SUCCESS);
        when(previousBuild.getResult()).thenReturn(Result.FAILURE);

        ViolationsBuildAction action = mock(ViolationsBuildAction.class);
        when(build.getActions(ViolationsBuildAction.class)).thenReturn(Arrays.asList(action));
        ViolationsReport previousReport = createViolationsReportStub("pmd", 10, null);
        ViolationsReport currentReport = createViolationsReportStub("pmd", 100, previousReport);
        when(action.getReport()).thenReturn(currentReport);

        RuleResult ruleResult = new DefaultViolationRule("pmd", "PMD violations", 100, -100).evaluate(build);
        assertNotNull("Rule result must not be null", ruleResult);
        assertThat("Points should be 0", ruleResult.getPoints(), is(0d));
    }

    /**
     * Creates a violation report stub with one TypeReport containing the method params
     * @param type type in the report
     * @param number the number of violations
     * @param previous if there is a previous report to be returned by report.getPrevious();
     * @return mocked ViolationsReport
     */
    private ViolationsReport createViolationsReportStub(String type, int number, final ViolationsReport previous){
        
        final ViolationsReport violationsReport = classContext.mock(ViolationsReport.class);        
        TypeReport typeReport = violationsReport.new TypeReport(type, null, number);
        
        final Map<String, TypeReport> typeReports = new HashMap<String, TypeReport>();
        typeReports.put(type, typeReport);
        
        classContext.checking(new Expectations() {
            {
                ignoring(violationsReport).getTypeReports(); will(returnValue(typeReports));
                ignoring(violationsReport).setBuild(with(any(Build.class)));
                if (previous != null) {
                    ignoring(violationsReport).previous(); will(returnValue(previous));
                }
            }
        });
        return violationsReport;
    }
}
