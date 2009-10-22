package hudson.plugins.im.bot;

import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.plugins.im.tools.MessageHelper;
import hudson.plugins.im.tools.Pair;
import hudson.security.AuthorizationStrategy;
import hudson.security.Permission;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Abstract command which returns a result message for one or several jobs.
 *
 * @author kutzi
 */
abstract class AbstractMultipleJobCommand extends AbstractTextSendingCommand {
	
	static final String UNKNOWN_JOB_STR = "unknown job";
	static final String UNKNOWN_VIEW_STR = "unknown view";

	private JobProvider jobProvider = new DefaultJobProvider();
	
	/**
	 * Returns the message to return for this job.
	 * Note that {@link AbstractMultipleJobCommand} already inserts one newline after each job's
	 * message so you don't have to do it yourself.
	 * 
	 * @param job The job
	 * @return the result message for this job
	 */
    protected abstract CharSequence getMessageForJob(AbstractProject<?, ?> job);

    /**
     * Returns a short name of the command needed for the help message
     * and as a leading descriptor in the result message.
     * 
     * @return short command name
     */
    protected abstract String getCommandShortName();
    
    enum Mode {
    	SINGLE, VIEW, ALL;
    }

    @Override
	protected String getReply(String sender, String[] args) {
    	
//    	if (!authorizationCheck()) {
//    		return "Sorry, can't do that!";
//    	}

        Collection<AbstractProject<?, ?>> projects = new ArrayList<AbstractProject<?, ?>>();

        final Pair<Mode, String> pair;
        try {
            pair = getProjects(sender, args, projects);
        } catch (CommandException e) {
            return getErrorReply(sender, e);
        }

        if (!projects.isEmpty()) {
            StringBuilder msg = new StringBuilder();
                
            switch(pair.getHead()) {
            	case SINGLE : break;
            	case ALL:
            		msg.append(getCommandShortName())
            			.append(" of all projects:\n");
            		break;
            	case VIEW:
            		msg.append(getCommandShortName())
        				.append(" of projects in view " + pair.getTail() + ":\n");
            		break;
            }

            boolean first = true;
            for (AbstractProject<?, ?> project : projects) {
                if (!first) {
                    msg.append("\n");
                } else {
                    first = false;
                }

                msg.append(getMessageForJob(project));
            }
            return msg.toString();
        } else {
            return sender + ": no job found";
        }
	}
    
	private boolean authorizationCheck() {
		if (Hudson.getInstance() == null) {
			// for testing
			return true;
		}
		AuthorizationStrategy strategy = Hudson.getInstance().getAuthorizationStrategy();
		return strategy.getACL(Hudson.getInstance()).hasPermission(Permission.READ);
	}
    
    /**
     * Returns a list of projects for the given arguments.
     * 
     * @param projects the list to which the projects are added
     * @return a pair of Mode (single job, jobs from view or all) and view name -
     * where view name will be null if mode != VIEW
     */
    Pair<Mode, String> getProjects(String sender, String[] args, Collection<AbstractProject<?, ?>> projects)
        throws CommandException {
        final Mode mode;
        String view = null;
        if (args.length >= 2) {
            if ("-v".equals(args[1])) {
                mode = Mode.VIEW;
                view = MessageHelper.getJoinedName(args, 2);
                getProjectsForView(projects, view);
            } else {
                mode = Mode.SINGLE;
                String jobName = MessageHelper.getJoinedName(args, 1);

                AbstractProject<?, ?> project = this.jobProvider.getJobByName(jobName);
                if (project != null) {
                    projects.add(project);
                } else {
                    throw new CommandException(sender + ": " + UNKNOWN_JOB_STR + " " + jobName);
                }
            }
        } else if (args.length == 1) {
            mode = Mode.ALL;
            for (AbstractProject<?, ?> project : this.jobProvider.getAllJobs()) {
                // add only top level project
                // sub project are accessible by their name but are not shown for visibility
                if (this.jobProvider.isTopLevelJob(project)) {
                    projects.add(project);
                }
            }
        } else {
            throw new CommandException(sender + ": 'args' must not be empty!");
        }
        return Pair.create(mode, view);
    }

	public String getHelp() {
        return " [<job>|-v <view>] - show the "
                + getCommandShortName()
                + " of a specific job, jobs in a view or all jobs";
    }

    private void getProjectsForView(Collection<AbstractProject<?, ?>> toAddTo, String viewName) {
        View view = this.jobProvider.getView(viewName);

        if (view != null) {
            Collection<TopLevelItem> items = view.getItems();
            for (TopLevelItem item : items) {
                if (item instanceof AbstractProject<?, ?>) {
                    toAddTo.add((AbstractProject<?, ?>) item);
                }
            }
        } else {
            throw new IllegalArgumentException(UNKNOWN_VIEW_STR + ": " + viewName);
        }
    }
    
    // for testing
    void setJobProvider(JobProvider jobProvider) {
        this.jobProvider = jobProvider;
    }
}
