package hudson.plugins.im;

public interface IMPublisherDescriptor {

	/**
	 * Returns <code>true</code> iff the plugin is globally enabled.
	 */
	boolean isEnabled();
	
	/**
	 * Returns an informal, short description of the concrete plugin.
	 */
	String getPluginDescription();
	
	/**
	 * Returns if the plugin should expose its presence on the IM network.
	 * I.e. if it should report as 'available' or that like.
	 */
    boolean isExposePresence();
    
    /**
     * Returns the hostname of the IM network. I.e. the host to which the plugin should connect.
     */
    String getHost();
    
    /**
     * Returns the hostname. May be null in which case the host must be determined from the
     * Jabber 'service name'.
     * 
     * @deprecated Should be replaced by getHost
     */
    @Deprecated
    String getHostname();
    
    /**
     * Returns the port of the IM network..
     */
    int getPort();
    
    /**
     * Returns the user name needed to login into the IM network.
     */
    String getUserName();
    
    /**
     * Returns the password needed to login into the IM network.
     */
    String getPassword();
    
    String getCommandPrefix();
 
    String getDefaultIdSuffix();
    
    /**
     * Returns the user name needed to login into Hudson.
     */
    String getHudsonUserName();
    
    /**
     * Returns the password needed to login into Hudson.
     */
    String getHudsonPassword();
}
