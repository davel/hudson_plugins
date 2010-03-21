package hudson.plugins.buckminster.install;

import hudson.tools.DownloadFromUrlInstaller.Installable;

public class BuckminsterInstallable extends Installable {
	
	//in case it does not get initalized from JSON (shouldn't happen)
	public Repository[] repositories = new Repository[0];
	public String repositoryURL;
	public String iu;
	
	public static class Feature
	{
		public String id;
	}

	public static class Repository{
		public String url;
		public Feature[] features;
	}

	public static class BuckminsterInstallableList{
		//in case it does not get initalized from JSON (shouldn't happen)
		public BuckminsterInstallable[] buckminsters = new BuckminsterInstallable[0];
	}
}
