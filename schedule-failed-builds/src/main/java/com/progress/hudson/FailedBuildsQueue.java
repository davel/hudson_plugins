package com.progress.hudson;

import java.util.HashMap;

import hudson.model.AbstractBuild;
import hudson.model.Build;
import hudson.model.BuildableItem;

/**
 * Helper class to keep track of failed builds
 * 
 * @author Stefan Fritz <sfritz@progress.com>
 *
 */
public class FailedBuildsQueue {
  
  private static HashMap<String, BuildItem> items=null;
  
  
  public static boolean needsBuild(BuildableItem job) {
    
    if(items!=null){      
      String fullName=job.getFullName();
      if( items.containsKey(fullName)){
        BuildItem item = items.get(fullName);
        return item.readyForBuild();
      }
    }
   
    return false;
  }

  public static void add(AbstractBuild build, String interval, String maxRetries) {
    if(items==null){
      items= new HashMap<String, BuildItem>();
    }
    String fullName=build.getParent().getFullName();
    if(!items.containsKey(fullName)){
      items.put(fullName, new BuildItem( build.getParent(),Integer.parseInt(interval), Integer.parseInt(maxRetries)));
    }
   
  }

  public static void remove(AbstractBuild build) {
    if(items!=null){      
      String fullName=build.getParent().getFullName();
      if( items.containsKey(fullName)){
        items.remove(fullName);
      }
    }
    
  }
  
  
    
}