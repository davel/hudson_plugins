/**
 * 
 */
package hudson.plugins.buckminster;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Johannes Utzig
 * 
 *@deprecated as of 0.9.4 (20.03.2010). Use {@link BuckminsterInstallation}
 *             instead
 * 
 */
@Deprecated
public class EclipseInstallation {

	private final String name, home, version, params;

	@DataBoundConstructor
	public EclipseInstallation(String name, String version, String home,
			String params) {
		super();
		this.name = name;
		this.home = home;
		this.version = version;
		this.params = params;
	}

	public String getParams() {
		return params;
	}

	public String getName() {
		return name;
	}

	public String getHome() {
		return home;
	}

	public String getVersion() {
		return version;
	}


}
