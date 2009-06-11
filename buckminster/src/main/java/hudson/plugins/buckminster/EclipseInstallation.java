/**
 * 
 */
package hudson.plugins.buckminster;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Johannes Utzig
 *
 */
public class EclipseInstallation {
	
	private final String name,home,version,params;

	@DataBoundConstructor
	public EclipseInstallation(String name, String version, String home, String params) {
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
