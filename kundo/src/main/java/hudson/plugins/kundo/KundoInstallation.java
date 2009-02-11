package hudson.plugins.kundo;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;

public final class KundoInstallation {
    private final String name;
    private final String kundoHome;

    @DataBoundConstructor
    public KundoInstallation( String name, String home ) {
        this.name = name;
        this.kundoHome = home;
    }

    /**
     * install directory.
     */
    public String getKundoHome() {
        return kundoHome;
    }

    /**
     * Human readable display name.
     */
    public String getName() {
        return name;
    }

    public File getExecutable() {
        String execType;
        if( File.separatorChar == '\\' ){
            execType = "kundo.bat";
        }
        else {
            execType = "kundo";
            
        }
        return new File( getKundoHome(), "bin/" + execType );
    }

    /**
     * Returns true if the executable exists.
     */
    public boolean getExists() {
        return getExecutable().exists();
    }
}
