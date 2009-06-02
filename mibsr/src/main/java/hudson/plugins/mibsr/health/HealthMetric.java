package hudson.plugins.mibsr.health;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA. User: stephen Date: 17-Mar-2008 Time: 12:43:47 To change this template use File | Settings
 * | File Templates.
 */
public interface HealthMetric<OBSERVABLE>
    extends Serializable
{

    String getName();

    float measure( OBSERVABLE observable );

    float getBest();

    float getWorst();
}
