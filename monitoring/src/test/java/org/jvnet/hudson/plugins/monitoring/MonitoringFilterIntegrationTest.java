package org.jvnet.hudson.plugins.monitoring;

import com.gargoylesoftware.htmlunit.Page;
import java.net.URL;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 *
 * @author Emeric Vernat
 */
public class MonitoringFilterIntegrationTest extends HudsonTestCase {
  public void test() throws Exception {
    WebClient wc = new WebClient();
    URL url = new URL(wc.getContextPath());
    Page page = wc.getPage(url);
    assertEquals(url, page.getWebResponse().getUrl());
  }
}
