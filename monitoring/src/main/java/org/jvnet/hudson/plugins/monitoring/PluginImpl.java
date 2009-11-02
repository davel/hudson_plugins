package org.jvnet.hudson.plugins.monitoring;

import hudson.Plugin;
import hudson.util.PluginServletFilter;

/**
 * Entry point of the plugin.
 *
 * <p>
 * There must be one {@link Plugin} class in each plugin.
 * See javadoc of {@link Plugin} for more about what can be done on this class.
 *
 * @author Emeric Vernat
 */
public class PluginImpl extends Plugin {

  @Override
  public void start() throws Exception {
    super.start();
	
	// on active les actions syst�mes (gc, heap dump, histogramme m�moire, processus...), sauf si l'administrateur a dit diff�remment
	if (System.getProperty("javamelody.system-actions-enabled") == null) {
		System.setProperty("javamelody.system-actions-enabled", "true");
	}
	// on d�sactive les graphiques jdbc et statistiques sql puisqu'il n'y en aura pas
	if (System.getProperty("javamelody.no-database") == null) {
		System.setProperty("javamelody.no-database", "true");
	}
	// le r�pertoire de stockage est dans le r�pertoire de hudson au lieu d'�tre dans le r�pertoire temporaire
	// ("/" initial n�cessaire sous windows pour javamelody v1.8.1)
	if (System.getProperty("javamelody.storage-directory") == null && System.getenv("HUDSON_HOME") != null) {
		System.setProperty("javamelody.storage-directory", "/" + System.getenv("HUDSON_HOME") + "/monitoring");
	}
	// google-analytics pour conna�tre le nombre d'installations actives et pour conna�tre les fonctions les plus utilis�es
	if (System.getProperty("javamelody.analytics-id") == null) {
		System.setProperty("javamelody.analytics-id", "UA-1335263-7");
	}
	
	PluginServletFilter.addFilter(new HudsonMonitoringFilter());
	
	// Rq: avec hudson, on ne peut pas ajouter un SessionListener comme dans un web.xml
	// TODO on pourrait ajouter un counter de nom job avec les temps de build
  }
}
