package de.fspengler.hudson.pview;

import hudson.Plugin;
import hudson.model.Hudson;

/**
 * Entry point of a plugin.
 *
 * <p>
 * There must be one {@link Plugin} class in each plugin.
 * See javadoc of {@link Plugin} for more about what can be done on this class.
 *
 * @author Tom Spengler
 */
public class PluginImpl extends Plugin {
	 
//	public static final UserPersonalViewPropertyDescriptor USER_PVIEW_EXPRESSION = new UserPersonalViewPropertyDescriptor();
	
	@Override
	public void start() throws Exception {
//		UserProperties.LIST.add(USER_PVIEW_EXPRESSION);
		
		Hudson.getInstance().getActions().add(new PViewLinkAction());
		
    }
}
