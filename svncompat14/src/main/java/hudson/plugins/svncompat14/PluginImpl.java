package hudson.plugins.svncompat14;

import hudson.Plugin;

import java.io.File;
import java.util.Collection;
import java.util.TreeSet;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.wc.admin.ISVNAdminAreaFactorySelector;
import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminArea14;
import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminAreaFactory;

/**
 * Plugin that allows SVNKit to be compatible with Subversion 1.4. All
 * Subversion operations in Hudson go through SVNKit, which creates working
 * copies only compatible with the most recent Subversion by default (currently
 * 1.5).
 * <p>
 * See {@link https://wiki.svnkit.com/SVNKit_FAQ} for more details.
 * <p>
 * This plugin is basically just an updated rip-off of the 'svncompat13' plugin
 * written by <a href="mailto:jbq@caraldi.com">Jean-Baptiste Quenot</a>.
 * 
 * @author <a href="mailto:simonwiest@simonwiest.de">Simon Wiest</a>
 * @plugin
 */
public class PluginImpl extends Plugin {

  @Override
  public void start() throws Exception {
    SVNAdminAreaFactory.setSelector(new ISVNAdminAreaFactorySelector() {

      public Collection getEnabledFactories(File path, Collection factories, boolean writeAccess) throws SVNException {
        // SVNKit does not pass in a typed collection, so casting is necessary to keep the following code clean.
        @SuppressWarnings( { "cast", "unchecked" })
        Collection<SVNAdminAreaFactory> typedFactories = (Collection<SVNAdminAreaFactory>) factories;

        Collection<SVNAdminAreaFactory> enabledFactories = new TreeSet<SVNAdminAreaFactory>();
        // Iterate over all available factories in the SVNKit distribution...
        for (SVNAdminAreaFactory factory : typedFactories) {
          // Enable only factories that use the 1.4 working copy format.
          if (factory.getSupportedVersion() == SVNAdminArea14.WC_FORMAT) {
            enabledFactories.add(factory);
          }
        }
        return enabledFactories;
      }
    });

  }
}
