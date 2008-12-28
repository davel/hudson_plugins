package hudson.plugins.testabilityexplorer.helpers;

import hudson.remoting.Callable;
import hudson.model.BuildListener;

/**
 * A {@link Callable} that will delegate parsing of the reports to the {@link ParseDelegate}.
 *
 * @author reik.schatz
 */
public class BuildProxyCallableHelper implements Callable<BuildProxy, Exception>
{
    private final BuildProxy m_buildProxy;
    private final ParseDelegate m_parseDelegate;
    private final BuildListener m_listener;

    public BuildProxyCallableHelper(BuildProxy buildProxy, ParseDelegate parseDelegate, BuildListener listener)
    {
        m_buildProxy = buildProxy;
        m_parseDelegate = parseDelegate;
        m_listener = listener;
    }

    /**
     * Delegates report parsing to the {@link ParseDelegate}. Might flags the {@link BuildProxy} as not being
     * successful.
     *
     * @return BuildProxy
     * @throws Exception
     */
    public BuildProxy call() throws Exception
    {
        m_parseDelegate.perform(m_buildProxy, m_listener);
        return m_buildProxy;
    }
}
