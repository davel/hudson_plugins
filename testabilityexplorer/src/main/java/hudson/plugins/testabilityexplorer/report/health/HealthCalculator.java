package hudson.plugins.testabilityexplorer.report.health;

/**
 * Calculates a health percentage based on the given number of excellent, good and poor classes.
 *
 * @author reik.schatz
 */
public interface HealthCalculator
{
    int calculate(int classes, int excellent, int good, int poor);
}
