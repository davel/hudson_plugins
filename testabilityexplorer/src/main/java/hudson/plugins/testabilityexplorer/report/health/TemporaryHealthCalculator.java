package hudson.plugins.testabilityexplorer.report.health;

/**
 * Calculates the health just based on the number of excellent classes.
 *
 * @author reik.schatz
 */
public class TemporaryHealthCalculator implements HealthCalculator
{
    public int calculate(int classes, int excellent, int good, int poor)
    {
        Double score = (((double) excellent) / (double) classes) * 100D;
        return score.intValue();
    }
}
