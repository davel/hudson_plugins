package hudson.plugins.mibsr.parser;

import hudson.model.AbstractBuild;
import hudson.util.IOException2;
import org.apache.maven.plugin.invoker.model.BuildJob;
import org.apache.maven.plugin.invoker.model.io.xpp3.BuildJobXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

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

    private SortedMap<String, BuildJob> buildJobs = new TreeMap<String, BuildJob>();

    private String name;

    public final Set<String> SUCCESSFUL = fixedTreeSet( BuildJob.Result.SUCCESS );

    public final Set<String> UNSUCCESSFUL =
        fixedTreeSet( BuildJob.Result.ERROR, BuildJob.Result.FAILURE_PRE_HOOK, BuildJob.Result.FAILURE_BUILD,
                      BuildJob.Result.FAILURE_POST_HOOK );

    public final Set<String> SKIPPED = fixedTreeSet( BuildJob.Result.SKIPPED );


    private static <T> Set<T> fixedTreeSet( T... elements )
    {
        return Collections.unmodifiableSet( new TreeSet<T>( Arrays.asList( elements ) ) );
    }


    /**
     * Gets the version of a string that's URL-safe.
     */
    public static String makeSafe( String unsafe )
    {
        StringBuffer buf = new StringBuffer( unsafe );
        for ( int i = 0; i < buf.length(); i++ )
        {
            char ch = buf.charAt( i );
            if ( !Character.isJavaIdentifierPart( ch ) )
            {
                buf.setCharAt( i, '_' );
            }
        }
        return buf.toString();
    }

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
                results.add( reader.read( bis ) );
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
        String key = makeSafe( r.getProject() );
        if ( buildJobs.containsKey( key ) )
        {
            int num = 1;
            while ( buildJobs.containsKey( key + num ) )
            {
                num++;
            }
            key = key + num;
        }
        buildJobs.put( key, r );
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
                merged.addAll( result.buildJobs.values() );
            }
            return merged;
        }
    }

// --------------------------- CONSTRUCTORS ---------------------------

    public BuildJobs()
    {
    }

    public BuildJob getDynamic( String name, StaplerRequest req, StaplerResponse resp )
    {
        return buildJobs.get( name );
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
        this.buildJobs = new TreeMap<String, BuildJob>( that.buildJobs );
    }

    public int getPassCount()
    {
        return getCount( BuildJob.Result.SUCCESS );
    }

    private int getCount( String... resultTypes )
    {
        int total = 0;
        for ( BuildJob result : buildJobs.values() )
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
        for ( BuildJob result : buildJobs.values() )
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
        for ( BuildJob result : buildJobs.values() )
        {
            total += result.getTime();
        }
        return Math.round( total * 10.0 ) / 10.0;
    }

    public Collection<BuildJob> getBuildJobs()
    {
        return buildJobs.values();
    }

    public void addAll( Collection<BuildJob> results )
    {
        for ( BuildJob job : results )
        {
            add( job );
        }
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

    public static enum ResultType
    {
        SUCCESS( BuildJob.Result.SUCCESS ),
        SKIPPED( BuildJob.Result.SKIPPED ),
        ERROR( BuildJob.Result.ERROR ),
        FAILURE_PRE_HOOK( BuildJob.Result.FAILURE_PRE_HOOK ),
        FAILURE_BUILD( BuildJob.Result.FAILURE_BUILD ),
        FAILURE_POST_HOOK( BuildJob.Result.FAILURE_POST_HOOK ),;

        private final String xmlValue;

        ResultType( String xmlValue )
        {
            this.xmlValue = xmlValue;
        }
    }
}
