package hudson.plugins.buckminster.targetPlatform;

/**
 * A reference to a target platform built by another job
 * 
 * @author Johannes Utzig
 *
 */
public class TargetPlatformReference {
	
	private String name;
	private String path;
	private String fullName;
	
	/**
	 * 
	 * @return the full and hopefully unique name of this target platform
	 */
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	/**
	 * 
	 * @return the human readable name for this target platform
	 */
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * @return the absolute path to this target platform
	 */
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	

}
