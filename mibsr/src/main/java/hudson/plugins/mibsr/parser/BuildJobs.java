package hudson.plugins.mibsr.parser;

import hudson.model.AbstractBuild;
import hudson.util.IOException2;
import org.apache.maven.plugin.invoker.model.BuildJob;
import org.apache.maven.plugin.invoker.model.io.xpp3.BuildJobXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 25-Feb-2008 21:33:40
 */
public class BuildJobs
    implements Serializable
{
// ------------------------------ FIELDS ------------------------------

    private AbstractBuild<?, ?> owner;

    private Collection<BuildJob> buildJobs = new ArrayList<BuildJob>();

    private String name;

// -------------------------- STATIC METHODS --------------------------

    public static BuildJobs parse( String[] inFiles )
        throws IOException
    {
        BuildJobs results = new BuildJobs();
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        BuildJobXpp3Reader reader = new BuildJobXpp3Reader();
        for ( String inFile : inFiles )
        {
            try
            {
                fis = new FileInputStream( inFile );
                bis = new BufferedInputStream( fis );
                results.buildJobs.add( reader.read( bis ) );
            }
            catch ( XmlPullParserException e )
            {
                throw new IOException2( "Could not parse build job buildJobs file " + inFile, e );
            }
            finally
            {
                if ( bis != null )
                {
                    bis.close();
                }
                if ( fis != null )
                {
                    fis.close();
                }
            }
        }
        return results;
    }

    public void add( BuildJob r )
    {
        buildJobs.add( r );
    }

    public static BuildJobs total( BuildJobs... results )
    {
        return total( Arrays.asList( results ) );
    }

    public static BuildJobs total( Collection<BuildJobs> results )
    {
        if ( results.isEmpty() )
        {
            return new BuildJobs();
        }
        else if ( results.size() == 1 )
        {
            return results.iterator().next();
        }
        else
        {
            BuildJobs merged = new BuildJobs();
            for ( BuildJobs result : results )
            {
                merged.buildJobs.addAll( result.buildJobs );
            }
            return merged;
        }
    }

// --------------------------- CONSTRUCTORS ---------------------------

    public BuildJobs()
    {
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public AbstractBuild<?, ?> getOwner()
    {
        return owner;
    }

    public void setOwner( AbstractBuild<?, ?> owner )
    {
        this.owner = owner;
    }

// ------------------------ CANONICAL METHODS ------------------------

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        BuildJobs buildJobs = (BuildJobs) o;

        if ( owner != null ? !owner.equals( buildJobs.owner ) : buildJobs.owner != null )
        {
            return false;
        }
        if ( this.buildJobs != null ? !this.buildJobs.equals( buildJobs.buildJobs ) : buildJobs.buildJobs != null )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = ( owner != null ? owner.hashCode() : 0 );
        return result;
    }

    public String toString()
    {
        return "BuildJobs{" + '}';
    }

    public String toSummary()
    {
        return "<ul>" + "Build Jobs" + "</ul>";
    }

    private static String diff( long a, long b, String name )
    {
        if ( a == b )
        {
            return "";
        }
        else if ( a < b )
        {
            return "<li>" + name + " (+" + ( b - a ) + ")</li>";
        }
        else
        { // if (a < b)
            return "<li>" + name + " (-" + ( a - b ) + ")</li>";
        }
    }

    public String toSummary( BuildJobs totals )
    {
        return "<ul>" + diff( totals.getTotalCount(), getTotalCount(), "integration tests" )
            + diff( totals.getPassCount(), getPassCount(), "successful" )
            + diff( totals.getSkipCount(), getSkipCount(), "skipped" )
            + diff( totals.getErrorCount(), getErrorCount(), "error" )
            + diff( totals.getFailCount(), getFailCount(), "total failing" )
            + diff( totals.getFailInitCount(), getFailInitCount(), "failing before run" )
            + diff( totals.getFailRunCount(), getFailRunCount(), "failing during run" )
            + diff( totals.getFailRunCount(), getFailRunCount(), "failing after run" ) + "</ul>";
    }

    public void set( BuildJobs that )
    {
        this.name = that.name;
        this.buildJobs = new ArrayList<BuildJob>( that.buildJobs );
    }

    public int getPassCount()
    {
        return getCount( BuildJob.Result.SUCCESS );
    }

    private int getCount( String... resultTypes )
    {
        int total = 0;
        for ( BuildJob result : buildJobs )
        {
            for ( String resultType : resultTypes )
            {
                if ( result.getResult().equalsIgnoreCase( resultType ) )
                {
                    total++;
                }
            }
        }
        return total;
    }

    private double getTime( String... resultTypes )
    {
        double total = 0;
        for ( BuildJob result : buildJobs )
        {
            for ( String resultType : resultTypes )
            {
                if ( result.getResult().equalsIgnoreCase( resultType ) )
                {
                    total += result.getTime();
                }
            }
        }
        return Math.round( total * 10.0 ) / 10.0;
    }

    public double getPassTime()
    {
        return getTime( BuildJob.Result.SUCCESS );
    }

    public int getSkipCount()
    {
        return getCount( BuildJob.Result.SKIPPED );
    }

    public double getSkipTime()
    {
        return getTime( BuildJob.Result.SKIPPED );
    }

    public int getErrorCount()
    {
        return getCount( BuildJob.Result.ERROR );
    }

    public double getErrorTime()
    {
        return getTime( BuildJob.Result.ERROR );
    }

    public int getFailInitCount()
    {
        return getCount( BuildJob.Result.FAILURE_PRE_HOOK );
    }

    public double getFailInitTime()
    {
        return getTime( BuildJob.Result.FAILURE_PRE_HOOK );
    }

    public int getFailRunCount()
    {
        return getCount( BuildJob.Result.FAILURE_BUILD );
    }

    public double getFailRunTime()
    {
        return getTime( BuildJob.Result.FAILURE_BUILD );
    }

    public int getFailValidateCount()
    {
        return getCount( BuildJob.Result.FAILURE_POST_HOOK );
    }

    public double getFailValidateTime()
    {
        return getTime( BuildJob.Result.FAILURE_POST_HOOK );
    }

    public int getFailCount()
    {
        return getCount( BuildJob.Result.FAILURE_PRE_HOOK, BuildJob.Result.FAILURE_BUILD,
                         BuildJob.Result.FAILURE_POST_HOOK );
    }

    public double getFailTime()
    {
        return getTime( BuildJob.Result.FAILURE_PRE_HOOK, BuildJob.Result.FAILURE_BUILD,
                        BuildJob.Result.FAILURE_POST_HOOK );
    }

    public int getTotalCount()
    {
        return buildJobs.size();
    }

    public double getTotalTime()
    {
        double total = 0;
        for ( BuildJob result : buildJobs )
        {
            total += result.getTime();
        }
        return Math.round( total * 10.0 ) / 10.0;
    }

    public Collection<BuildJob> getBuildJobs()
    {
        return buildJobs;
    }

    public void addAll( Collection<BuildJob> results )
    {
        buildJobs.addAll( results );
    }

    public void clear()
    {
        buildJobs.clear();
    }

    public String getCssClass( BuildJob result )
    {
        if ( BuildJob.Result.SUCCESS.equalsIgnoreCase( result.getResult() ) )
        {
            return "result-passed";
        }
        if ( BuildJob.Result.SKIPPED.equalsIgnoreCase( result.getResult() ) )
        {
            return "result-skipped";
        }
        if ( BuildJob.Result.ERROR.equalsIgnoreCase( result.getResult() ) )
        {
            return "result-failed";
        }
        return "result-failed";
    }
}
