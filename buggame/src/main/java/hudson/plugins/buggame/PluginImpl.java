package hudson.plugins.buggame;

import hudson.Extension;
import hudson.Plugin;

/**
 * Entry point of the Bug Game plugin.
 */
@Extension
public class PluginImpl extends Plugin {

    @Override
    public void start() throws Exception {
        /*
         * List<UserInfo> users = Hudson.getInstance().getPeople().users;
         * System.out.println("USERS = " + users.size()); for (UserInfo userInfo :
         * users) { UserScoreProperty property =
         * userInfo.getUser().getProperty(UserScoreProperty.class); if (property !=
         * null) { Hudson.getInstance().getActions().add(new
         * LeaderBoardAction()); break; } }
         */
    }
}
