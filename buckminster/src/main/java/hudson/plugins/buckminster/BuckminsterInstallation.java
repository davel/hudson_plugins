/**
 * 
 */
package hudson.plugins.buckminster;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.EnvironmentSpecific;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.model.DownloadService.Downloadable;
import hudson.plugins.buckminster.command.CommandLineBuilder;
import hudson.plugins.buckminster.install.BuckminsterInstallable;
import hudson.plugins.buckminster.install.BuckminsterInstallable.BuckminsterInstallableList;
import hudson.slaves.NodeSpecific;
import hudson.tools.DownloadFromUrlInstaller;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolProperty;
import hudson.util.TextFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Johannes Utzig
 *
 */
public class BuckminsterInstallation extends ToolInstallation implements EnvironmentSpecific<BuckminsterInstallation>, NodeSpecific<BuckminsterInstallation> {
	

	private String version;
	private String params;

	@DataBoundConstructor
	public BuckminsterInstallation(String name, String home, String version, String params, List<ToolProperty<?>> properties) {
		super(name, home, properties);
		this.version = version;
		this.params = params;
	}

	public String getParams() {
		return params;
	}

	public String getVersion() {
		return version;
	}

    public BuckminsterInstallation forEnvironment(EnvVars environment) {
        return new BuckminsterInstallation(getName(), environment.expand(getHome()), version, params, getProperties().toList());
    }

    public BuckminsterInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        return new BuckminsterInstallation(getName(), translateFor(node, log), version, params, getProperties().toList());
    }
    
    public boolean exists()
    {
    	String home = getHome();
    	File f = new File(home);
    	File buckyDir = new File(f,"buckminster");
    	if(!buckyDir.exists())
    		return false;
    	File executableWin = new File(buckyDir,"buckminster.bat");
    	File executableUnix = new File(buckyDir,"buckminster");
    	return executableWin.exists() || executableUnix.exists();
    }
    
    @Extension
    public static class DescriptorImpl extends ToolDescriptor<BuckminsterInstallation>
    {

		@Override
		public String getDisplayName() {
			return "Buckminster";
		}
		
		@Override
		public List<? extends ToolInstaller> getDefaultInstallers() {
			return Collections.singletonList(new BuckminsterInstaller(null));
		}
//		
        // for compatibility reasons, the persistence is done by Ant.DescriptorImpl  
        @Override
        public BuckminsterInstallation[] getInstallations() {
            return Hudson.getInstance().getDescriptorByType(EclipseBuckminsterBuilder.DescriptorImpl.class).getBuckminsterInstallations();
        }

        @Override
        public void setInstallations(BuckminsterInstallation... installations) {
        	Hudson.getInstance().getDescriptorByType(EclipseBuckminsterBuilder.DescriptorImpl.class).setBuckminsterInstallations(installations);
        }
	
    }

	public static class BuckminsterInstaller extends DownloadFromUrlInstaller {

		@DataBoundConstructor
		public BuckminsterInstaller(String id) {
			super(id);
		}

		@Override
		protected FilePath findPullUpDirectory(FilePath root)
				throws IOException, InterruptedException {
			return null;
		}
		
		@Override
		public FilePath performInstallation(ToolInstallation tool, Node node,
				TaskListener log) throws IOException, InterruptedException {
			FilePath director = super.performInstallation(tool, node, log);
			FilePath buckminsterDir = director.child("buckminster");
			if(buckminsterDir.exists())
			{
		    	FilePath executableWin = buckminsterDir.child("buckminster.bat");
		    	FilePath executableUnix = buckminsterDir.child("buckminster");
		    	if(executableUnix.exists() || executableWin.exists())
				//	here we could do an update...
		    		return buckminsterDir;
			}
//	        FilePath expected = preferredLocation(tool, node);
	        BuckminsterInstallable inst = (BuckminsterInstallable) getInstallable();
	        String command = CommandLineBuilder.createInstallScript(inst, director, node, log);
	        FilePath script = buckminsterDir.createTextTempFile("hudson", ".sh", command);
	        
	        try {
	            String[] cmd = {"sh", "-e", script.getRemote()};
	            int r = node.createLauncher(log).launch().cmds(cmd).stdout(log).pwd(buckminsterDir).join();
	            if (r != 0) {
	                throw new IOException("Command returned status " + r);
	            }
	        } finally {
	            script.delete();
	        }
	        return buckminsterDir;
			
		}
		
		@Extension
		public static final class DescriptorImpl extends
				DownloadFromUrlInstaller.DescriptorImpl<BuckminsterInstaller> {
			public String getDisplayName() {
				return "Install from Eclipse.org and Cloudsmith.com";
			}

			@Override
			public boolean isApplicable(
					Class<? extends ToolInstallation> toolType) {
				return toolType == BuckminsterInstallation.class;
			}

			
			//this is for testing only
//			@Override
//			protected Downloadable createDownloadable() {
//				return new Downloadable(getId()) {
//					@Override
//					public String getUrl() {
//						return "file://home/joe/workspaceHudson2/buckminster/test.json";
//					}
//					
//				    public TextFile getDataFile() {
//				    	File f = new File("/home/joe/workspaceHudson2/buckminster/test.json");
//				        return new TextFile(f);
//				    }
//				};
//			}
			@Override
			public List<? extends Installable> getInstallables()
					throws IOException {
	            JSONObject d = Downloadable.get(getId()).getData();
	            if(d==null)     return Collections.emptyList();
	            return Arrays.asList(((BuckminsterInstallableList)JSONObject.toBean(d,BuckminsterInstallableList.class)).buckminsters);

			}
			
		}

	}
    
}
