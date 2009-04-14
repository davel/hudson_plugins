package hudson.plugins.pxe.ubuntu;

import hudson.Extension;
import hudson.Util;
import hudson.plugins.pxe.BootConfiguration;
import hudson.plugins.pxe.BootConfigurationDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.jvnet.hudson.tftpd.Data;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Ubuntu boot configuration.
 *
 * @author Kohsuke Kawaguchi
 */
public class UbuntuBootConfiguration extends BootConfiguration {
    public final Release release;

    public UbuntuBootConfiguration(Release release) {
        this.release = release;
    }

    @DataBoundConstructor
    public UbuntuBootConfiguration(String release) {
        this.release = Release.get(release);
    }

    public String getPxeLinuxConfigFragment() throws IOException {
        return String.format("LABEL %1$s\n" +
                "    MENU LABEL Ubuntu %2$s (%3$s)\n" +
                "    KERNEL vesamenu.c32\n" +
                "    APPEND %4$s/menu.txt \n",
                hashCode(), release.number, release.nickName, hashCode());
    }

    /**
     * Serves menu.txt by replacing variables.
     */
    public Data tftp(String fileName) throws IOException {
        Matcher m = MENU_CONFIG.matcher(fileName);
        if(!m.matches())    return null;
        if(!m.group(1).equals(String.valueOf(hashCode())))   return null;

        String template = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("tftp/ubuntu/menu.txt"));
        return Data.from(Util.replaceMacro(template, Collections.singletonMap("RELEASE",release.nickName)));
    }

    private static final Pattern MENU_CONFIG = Pattern.compile("(.+)/menu.txt");

    @Extension
    public static class DescriptorImpl extends BootConfigurationDescriptor {
        public String getDisplayName() {
            return "Ubuntu";
        }

        public List<Release> getReleases() {
            return Release.RELEASES;
        }
    }
}
