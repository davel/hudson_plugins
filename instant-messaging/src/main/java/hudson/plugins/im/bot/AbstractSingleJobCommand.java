package hudson.plugins.im.bot;

import hudson.model.AbstractProject;
import hudson.plugins.im.tools.ExceptionHelper;
import hudson.plugins.im.tools.MessageHelper;

/**
 * Abstract job which works on a single job - without taking any further arguments.
 *
 * @author kutzi
 */
abstract class AbstractSingleJobCommand extends AbstractTextSendingCommand {

    private final int numberOfArguments;
    
    private JobProvider jobProvider = new DefaultJobProvider();

    protected AbstractSingleJobCommand() {
        this(0);
    }
    
    /**
     * @param numberOfArguments The number of arguments (in addition to the job name)
     * required by this command. Number of actual specified arguments may be equal or greater.
     */
    protected AbstractSingleJobCommand(int numberOfArguments) {
        this.numberOfArguments = numberOfArguments;
    }

    /**
     * Returns the message to return for this job.
     * 
     * Implementors should only return a String if the command
     * was executed successfully. Otherwise a {@link CommandException}
     * should be thrown!
     * 
     * @param job The job
     * @param sender The sender of the command
     * @return the result message for this job if the command was executed successfully
     * @throws CommandException if the command couldn't be executed for any reason
     */
    protected abstract CharSequence getMessageForJob(AbstractProject<?, ?> job, String sender,
            String[] arguments) throws CommandException;

    @Override
    protected String getReply(String sender, String[] args) {
        if (args.length > 1 + numberOfArguments) {
            final String jobName;
            final String[] remainingArgs;
            if (this.numberOfArguments == 0) {
                jobName  = MessageHelper.getJoinedName(args, 1);
                remainingArgs = new String[0];
            } else {
                jobName = args[1].replace("\"", "");
                remainingArgs = MessageHelper.copyOfRange(args, 2, args.length);
            }
            AbstractProject<?, ?> job = this.jobProvider.getJobByName(jobName);
            if (job != null) {
                try {
                    return getMessageForJob(job, sender, remainingArgs).toString();
                } catch (CommandException e) {
                    return getErrorReply(sender, e);
                }
            } else {
                return sender + ": unknown job '" + jobName + "'";
            }
        } else {
            if (this.numberOfArguments == 0) {
                return sender + ": you must specify a job name";
            } else {
                return sender + ": you must specify a job name and " + this.numberOfArguments +
                 " additional arguments";
            }
        }
    }
    
    private String getErrorReply(String sender, CommandException e) {
        final StringBuilder reply;
        if(e.getReplyMessage() != null) {
            reply = new StringBuilder(e.getReplyMessage()).append("\n");
        } else {
            reply = new StringBuilder(sender).append(": command couldn't be executed. Error:\n");
        }
        if(e.getCause() != null) {
            reply.append("Cause: ").append(ExceptionHelper.dump(e.getCause()));
        }
        return reply.toString();
    }

    // for testing
    void setJobProvider(JobProvider jobProvider) {
        this.jobProvider = jobProvider;
    }
}
