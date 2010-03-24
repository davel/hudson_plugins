package hudson.plugins.cpptest;

import hudson.plugins.analysis.core.AbstractHealthDescriptor;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.util.model.AnnotationProvider;

import org.jvnet.localizer.Localizable;

/**
 * A health descriptor for Cpptest build results.
 *
 * @author Ulli Hafner
 * 
 * NQH: adapt for Cpptest
 */
public class CpptestHealthDescriptor extends AbstractHealthDescriptor {
    /** Unique ID of this class. */
    private static final long serialVersionUID = -3404826986876607396L;

    /**
     * Creates a new instance of {@link CpptestHealthDescriptor} based on the
     * values of the specified descriptor.
     *
     * @param healthDescriptor the descriptor to copy the values from
     */
    public CpptestHealthDescriptor(final HealthDescriptor healthDescriptor) {
        super(healthDescriptor);
    }

    /** {@inheritDoc} */
    @Override
    protected Localizable createDescription(final AnnotationProvider result) {
        if (result.getNumberOfAnnotations() == 0) {
            return Messages._Cpptest_ResultAction_HealthReportNoItem();
        }
        else if (result.getNumberOfAnnotations() == 1) {
            return Messages._Cpptest_ResultAction_HealthReportSingleItem();
        }
        else {
            return Messages._Cpptest_ResultAction_HealthReportMultipleItem(result.getNumberOfAnnotations());
        }
    }
}

