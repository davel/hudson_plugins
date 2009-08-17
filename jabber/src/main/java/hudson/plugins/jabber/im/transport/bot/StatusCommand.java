/*
 * Created on Apr 22, 2007
 */
package hudson.plugins.jabber.im.transport.bot;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.jabber.tools.MessageHelper;




/**
 * Job/project status command for the jabber bot
 * @author Pascal Bleser
 */
public class StatusCommand extends JobOverviewCommand {

    @Override
    protected CharSequence getMessageForJob(AbstractProject<?, ?> project) {
        StringBuilder msg = new StringBuilder(32);
        msg.append(project.getName());
        if (project.isDisabled()) {
            msg.append("(disabled) ");
        // a project which is building and additionally in queue should be reported as building
        } else if (project.isBuilding()) {
            msg.append("(BUILDING ").append(project.getLastBuild().getDurationString()).append(")");
        } else if (project.isInQueue()) {
            msg.append("(in queue) ");
        }
        msg.append(": ");

        AbstractBuild<?, ?> lastBuild = project.getLastBuild();
        while ((lastBuild != null) && lastBuild.isBuilding()) {
            lastBuild = lastBuild.getPreviousBuild();
        }
        if (lastBuild != null) {
            msg.append("last build: ").append(lastBuild.getNumber()).append(" (")
            	.append(lastBuild.getTimestampString()).append(" ago): ").append(lastBuild.getResult()).append(": ")
            	.append(MessageHelper.getBuildURL(lastBuild));
        } else {
            msg.append("no finished build yet");
        }
        return msg;
    }

    @Override
    protected String getCommandShortName() {
        return "status";
    }
}
