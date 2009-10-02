package hudson.plugins.polarion;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SubversionChangeLogSet.LogEntry;
import hudson.scm.SubversionChangeLogSet.Path;
import hudson.scm.SubversionRepositoryBrowser;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
/**
 * {@link SubversionRepositoryBrowser} that produces links to the Polarion Web Client for SVN
 *
 * @author Jonny Wray
 */
public class PolarionRepositoryBrowser extends SubversionRepositoryBrowser {

	private static final String CHANGE_SET_FORMAT = "revisionDetails.jsp?location=%s&rev=%d";
	private static final String DIFF_FORMAT = "changedResource.jsp?location=%s&url=%s&rev=%d&action=%s";
	private static final String FILE_FORMAT = "fileContent.jsp?location=%s&url=%s";

	private static final Map editTypeMap = new HashMap();
	static{
		editTypeMap.put(EditType.ADD, "add");
		editTypeMap.put(EditType.EDIT, "modify");
		editTypeMap.put(EditType.DELETE, "delete");
		// no replace in EditType which polarion has an action=replace for.
	};
	public final URL url;
	private final String location;

    @DataBoundConstructor
    public PolarionRepositoryBrowser(URL url, String location) throws MalformedURLException {
		this.url = normalizeToEndWithSlash(url);
		this.location = location;
	}

    public String getLocation() {
        if(location==null)  return "/";
        return location;
    }

    @Override
    public URL getDiffLink(Path path) throws IOException {
		if(!editTypeMap.containsKey(path.getEditType())){
			return null;
		}
		String editType = (String)editTypeMap.get(path.getEditType());
		return new URL(url, String.format(DIFF_FORMAT, getLocation(), path.getValue(), path.getLogEntry().getRevision(), editType));
    }

    @Override
    public URL getFileLink(Path path) throws IOException {
    	return new URL(url, String.format(FILE_FORMAT, getLocation(), path.getValue()));
    }

    @Override
    public URL getChangeSetLink(LogEntry changeSet) throws IOException {
    	return new URL(url, String.format(CHANGE_SET_FORMAT, getLocation(), changeSet.getRevision()));
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {
        public DescriptorImpl() {
            super(PolarionRepositoryBrowser.class);
        }

        public String getDisplayName() {
            return "Polarion Web Client";
        }

        @Override
        public PolarionRepositoryBrowser newInstance(StaplerRequest req, JSONObject formData) throws FormException {
		   return req.bindParameters(PolarionRepositoryBrowser.class, "polarion.");
        }
    }
}
