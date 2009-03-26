package hudson.plugins.easyant;

import java.io.File;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 *	This class represent an easyant installation 
 * @author Jean Louis Boudart
 */
public final class EasyAntInstallation {
	private final String name;
	private final String easyantHome;

	@DataBoundConstructor
	public EasyAntInstallation(String name, String easyantHome) {
		this.name = name;
		this.easyantHome = easyantHome;
	}

	public String getName() {
		return name;
	}

	public String getEasyantHome() {
		return easyantHome;
	}
	
	public File getExecutable() {
		String execName;
		if (File.separatorChar=='\\') {
			execName="easyant.bat";
		} else {
			execName="easyant";
		}
		return new File(getEasyantHome(),"bin/"+execName);
	}
	
	public boolean isAvailable() {
		return getExecutable().exists();
	}

}
