package hudson.plugins.mibsr;

import hudson.plugins.helpers.health.HealthMetric;
import org.apache.commons.beanutils.Converter;

/**
 * Created by IntelliJ IDEA. User: stephen Date: 18-Mar-2008 Time: 06:04:17 To change this template use File | Settings
 * | File Templates.
 */
public enum MIBSRHealthMetrics
    implements HealthMetric<MIBSRBuildIndividualReport>
{

    TEST_COUNT
        {

            public String getName()
            {
                return "Dummy";
            }
            public float measure( MIBSRBuildIndividualReport report )
            {
                return 100;
            }
            public float getBest()
            {
                return 100;
            }
            public float getWorst()
            {
                return 0;
            }
        },;

    static Converter CONVERTER = new Converter()
    {
        public Object convert( Class aClass, Object o )
        {
            return valueOf( o.toString() );
        }
    };
}
