package hudson.plugins.createjobadvanced;

import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.listeners.ItemListener;
import hudson.security.AuthorizationMatrixProperty;
import hudson.security.Permission;
import hudson.security.SecurityMode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.map.HashedMap;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @plugin
 * @author Bertrand Gressier
 *
 */
@Extension
public class CreateJobAdvanced extends ItemListener {
	
	static Logger log = Logger.getLogger(CreateJobAdvanced.class.getName());
	
	@DataBoundConstructor
	public CreateJobAdvanced() {
		log.info("Create job advanced started");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreated(Item item) {
		
		if (! (item instanceof Job))
			return;
		
		//hudson must activate security mode for using
		 if (Hudson.getInstance().getSecurity().equals(SecurityMode.UNSECURED))
			return;
		
		Job job = (Job)item;
		
		Map<Permission, Set<String>> permissions = null;
		
		//if you create the job with template, need to get informations
		AuthorizationMatrixProperty auth = (AuthorizationMatrixProperty)job.getProperty(AuthorizationMatrixProperty.class);
		if (auth != null){
			permissions = new HashMap<Permission, Set<String>>(auth.getGrantedPermissions());
			try {
				job.removeProperty(AuthorizationMatrixProperty.class);
			} catch (IOException e) {
				log.log(Level.SEVERE,"problem to remove granted permissions (template or copy job)",e);
			}
		}else {
			permissions= new HashMap<Permission, Set<String>>();
		}
		
		String sid = Hudson.getAuthentication().getName();
		
		configurePermission(permissions, Item.CONFIGURE, sid);
		configurePermission(permissions, Item.READ, sid);
		configurePermission(permissions, Item.BUILD, sid);
		configurePermission(permissions, Item.WORKSPACE, sid);
		configurePermission(permissions, Item.DELETE, sid);
		
		try {
			AuthorizationMatrixProperty authProperty =new AuthorizationMatrixProperty(permissions);
			job.addProperty(authProperty);
			log.info("Create Job " + item.getDisplayName() +" with right on " + sid);
		} catch (IOException e) {
			log.log(Level.SEVERE,"problem to add granted permissions",e);
		}
	}
	
	private void configurePermission(Map<Permission, Set<String>> permissions, Permission permission , String sid){
		
		Set<String> sidPermission = permissions.get(permission);
		if (sidPermission == null){
			Set<String> sidSet = new HashSet<String>();
			sidSet.add(sid);
			permissions.put(permission,sidSet);
		}else{
			if (!sidPermission.contains(sid)){
				sidPermission.add(sid);
			}
		}
	}
}
