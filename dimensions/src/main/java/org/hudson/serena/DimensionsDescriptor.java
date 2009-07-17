package org.hudson.serena;

import org.hudson.serena.model.Dimension10Installation;
import com.serena.dmclient.api.DimensionsConnection;
import hudson.Extension;
import hudson.model.ModelObject;
import hudson.scm.SCMDescriptor;
import hudson.util.FormFieldValidator;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Contains the list of configured Dimensions servers.
 *
 * @author Jose Noheda [jose.noheda@gmail.com]
 */
@Extension
public class DimensionsDescriptor extends SCMDescriptor<DimensionsSCM> implements ModelObject {

    private static final Logger LOGGER = Logger.getLogger(DimensionsDescriptor.class.getName());

    public static final DimensionsDescriptor DESCRIPTOR = new DimensionsDescriptor();

    private List<Dimension10Installation> installations;

    public DimensionsDescriptor() {
         super(DimensionsSCM.class, null);
         this.load();
    }

    @Override public final String getDisplayName() {
        return "Dimensions 10";
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(DESCRIPTOR, json);
        DESCRIPTOR.save();
        return true;
    }

    /**
     * Tests the connection to a Dimensions server with the provided parameters. Useful for Form Validation.
     */
    public void doTestConnection(StaplerRequest req, StaplerResponse rsp, @QueryParameter("server") final String server, @QueryParameter("dbName") final String dbName, @QueryParameter("dbConnection") final String dbConnection, @QueryParameter("user") final String user, @QueryParameter("password") final String password) throws IOException, ServletException {
        LOGGER.info("Testing connection " + server + ":" + dbName + "@" + dbConnection + ":" + user);
        new FormFieldValidator(req, rsp, true) {
            protected void check() throws IOException, ServletException {
                try {
                    DimensionsConnection conn = ConnectionManager.getConnection(server, dbName, dbConnection, user, password);
                    conn.initialise();
                    ConnectionManager.close(conn);
                    ok("Connection to [" + server + "] successful");
                } catch (Exception e) {
                    error("Connection error : " + e.getMessage());
                }
            }
        }.process();
    }

    /**
     * Returns server connection data given an identifier.
     */
    public Dimension10Installation getInstallation(String name) {
        if ((getInstallations() != null) && (name != null)) {
            for (Dimension10Installation installation : getInstallations()) {
                if ((installation.getName() != null) && (installation.getName().equals(name))) {
                    return installation;
                }
            }
        }
        return null;
    }

    public List<Dimension10Installation> getInstallations() {
        return DESCRIPTOR.installations;
    }

    public void setInstallations(List<Dimension10Installation> installations) {
        DESCRIPTOR.installations = installations;
    }

}
