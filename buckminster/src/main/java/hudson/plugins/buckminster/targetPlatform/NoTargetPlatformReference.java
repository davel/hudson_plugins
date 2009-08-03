package hudson.plugins.buckminster.targetPlatform;

/**
 *
 * Instances of this class represent that no target platform is referenced.
 * 
 * @author Johannes Utzig
 *
 */
public class NoTargetPlatformReference extends TargetPlatformReference {
	@Override
	public String getName() {
		return "None";
	}
}
