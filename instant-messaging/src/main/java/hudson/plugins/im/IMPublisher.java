package hudson.plugins.im;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.User;
import hudson.plugins.im.tools.Assert;
import hudson.plugins.im.tools.BuildHelper;
import hudson.plugins.im.tools.ExceptionHelper;
import hudson.plugins.im.tools.MessageHelper;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.BuildStep;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The actual Publisher that sends notification-Messages out to the clients.
 * @author Uwe Schaefer
 *
 */
public abstract class IMPublisher extends Notifier implements BuildStep
{
	private static final Logger LOGGER = Logger.getLogger(IMPublisher.class.getName());
	
    private static final IMMessageTargetConverter CONVERTER = new DefaultIMMessageTargetConverter();
    
    private List<IMMessageTarget> targets = new LinkedList<IMMessageTarget>();
    
    /**
     * @deprecated only left here to deserialize old configs
     */
    @Deprecated
	private hudson.plugins.jabber.NotificationStrategy notificationStrategy;
    
    private NotificationStrategy strategy;
    private final boolean notifyOnBuildStart;
    private final boolean notifySuspects;
    private final boolean notifyCulprits;
    private final boolean notifyFixers;
    private final boolean notifyUpstreamCommitters;
    
    /**
     * @deprecated Only for deserializing old instances
     */
    @SuppressWarnings("unused")
    @Deprecated
    private transient String defaultIdSuffix;

    protected IMPublisher(final String targetsAsString, final String notificationStrategyString,
    		final boolean notifyGroupChatsOnBuildStart,
    		final boolean notifySuspects,
    		final boolean notifyCulprits,
    		final boolean notifyFixers,
    		final boolean notifyUpstreamCommitters) throws IMMessageTargetConversionException
    {
        Assert.isNotNull(targetsAsString, "Parameter 'targetsAsString' must not be null.");
        setTargets(targetsAsString);

        NotificationStrategy strategy = NotificationStrategy.forDisplayName(notificationStrategyString);
        if (strategy == null) {
        	strategy = NotificationStrategy.STATECHANGE_ONLY;
        }
        this.strategy = strategy;
        
        this.notifyOnBuildStart = notifyGroupChatsOnBuildStart;
        this.notifySuspects = notifySuspects;
        this.notifyCulprits = notifyCulprits;
        this.notifyFixers = notifyFixers;
        this.notifyUpstreamCommitters = notifyUpstreamCommitters;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needsToRunAfterFinalized() {
        // notifyUpstreamCommitters needs the fingerprints to be generated
        // which seems to happen quite late in the build
        return this.notifyUpstreamCommitters;
    }
    
    /**
     * Returns a short name of the plugin to be used e.g. in log messages.
     */
    protected abstract String getPluginName();
    
    protected abstract IMConnection getIMConnection() throws IMException;

    protected IMMessageTargetConverter getIMMessageTargetConverter() {
        return IMPublisher.CONVERTER;
    }

    protected NotificationStrategy getNotificationStrategy() {
        return strategy;
    }
    
    protected void setNotificationStrategy(NotificationStrategy strategy) {
    	this.strategy = strategy;
    }

    protected List<IMMessageTarget> getNotificationTargets() {
        return this.targets;
    }

    /**
     * Returns the notification targets as a string suitable for
     * display in the settings page.
     *
     * Returns an empty string if no targets are set.
     */
    public String getTargets() {
    	if (this.targets == null) {
    		return "";
    	}

        final StringBuilder sb = new StringBuilder();
        for (final IMMessageTarget t : this.targets) {
            sb.append(getIMMessageTargetConverter().toString(t));
            sb.append(" ");
        }
        return sb.toString().trim();
    }
	
    protected void setTargets(String targetsAsString) throws IMMessageTargetConversionException {
    	this.targets = new LinkedList<IMMessageTarget>();
    	
        final String[] split = targetsAsString.split("\\s");
        final IMMessageTargetConverter conv = getIMMessageTargetConverter();
        for (final String fragment : split)
        {
            IMMessageTarget createIMMessageTarget;
            createIMMessageTarget = conv.fromString(fragment);
            if (createIMMessageTarget != null)
            {
                this.targets.add(createIMMessageTarget);
            }
        }
	}
    
    public final String getStrategy() {
        return getNotificationStrategy().getDisplayName();
    }
    
    public final boolean getNotifyOnStart() {
    	return notifyOnBuildStart;
    }
    
    public final boolean getNotifySuspects() {
    	return notifySuspects;
    }
    
    public final boolean getNotifyCulprits() {
    	return notifyCulprits;
    }

    public final boolean getNotifyFixers() {
    	return notifyFixers;
    }
    
    public final boolean getNotifyUpstreamCommitters() {
        return notifyUpstreamCommitters;
    }
    
    protected void log(BuildListener listener, String message) {
    	listener.getLogger().append(getPluginName()).append(": ").append(message).append("\n");
    }

    @Override
    public boolean perform(final AbstractBuild<?,?> build, final Launcher launcher, final BuildListener buildListener)
            throws InterruptedException, IOException
    {
        Assert.isNotNull(build, "Parameter 'build' must not be null.");
        Assert.isNotNull(buildListener, "Parameter 'buildListener' must not be null.");
        if (getNotificationStrategy().notificationWanted(build)) {
            notifyChats(build, buildListener);
        }

        if (BuildHelper.isStillFailureOrUnstable(build)) {
            if (this.notifySuspects) {
            	log(buildListener, "Notifying suspects");
            	final String message = "Build " + build.getProject().getName() +
            	    " is " + BuildHelper.getResultDescription(build) + ": " + MessageHelper.getBuildURL(build);
            	
            	for (IMMessageTarget target : calculateIMTargets(getCommitters(build), buildListener)) {
            		try {
            			log(buildListener, "Sending notification to suspect: " + target.toString());
            			getIMConnection().send(target, message);
            		} catch (final Throwable e) {
            			log(buildListener, "There was an error sending suspect notification to: " + target.toString());
            		}
            	}
            }
            
            if (this.notifyCulprits) {
            	log(buildListener, "Notifying culprits");
            	final String message = "You're still being suspected of having broken " + build.getProject().getName() + ": " + MessageHelper.getBuildURL(build);
            	
            	for (IMMessageTarget target : calculateIMTargets(getCulpritsOnly(build), buildListener)) {
            		try {
            			log(buildListener, "Sending notification to culprit: " + target.toString());
            			getIMConnection().send(target, message);
            		} catch (final Throwable e) {
            			log(buildListener, "There was an error sending culprit notification to: " + target.toString());
            		}
            	}
            }
        } else if (BuildHelper.isFailureOrUnstable(build)) {
            boolean committerNotified = false;
            if (this.notifySuspects) {
                log(buildListener, "Notifying suspects");
                String message = "Oh no! You're suspected of having broken " + build.getProject().getName() + ": " + MessageHelper.getBuildURL(build);
                
                for (IMMessageTarget target : calculateIMTargets(getCommitters(build), buildListener)) {
                    try {
                        log(buildListener, "Sending notification to suspect: " + target.toString());
                        getIMConnection().send(target, message);
                        committerNotified = true;
                    } catch (final Throwable e) {
                        log(buildListener, "There was an error sending suspect notification to: " + target.toString());
                    }
                }
            }
            
            if (this.notifyUpstreamCommitters && !committerNotified) {
                notifyUpstreamCommitters(build, buildListener);
            }
        }
        
        if (this.notifyFixers && BuildHelper.isFix(build)) {
        	buildListener.getLogger().append("Notifying fixers\n");
        	final String message = "Yippie! Seems you've fixed " + build.getProject().getName() + ": " + MessageHelper.getBuildURL(build);
        	
        	for (IMMessageTarget target : calculateIMTargets(getCommitters(build), buildListener)) {
        		try {
        			log(buildListener, "Sending notification to fixer: " + target.toString());
        			getIMConnection().send(target, message);
        		} catch (final Throwable e) {
        			log(buildListener, "There was an error sending fixer notification to: " + target.toString());
        		}
        	}
        }
        
        return true;
    }

    /**
     * Looks for committers in the direct upstream builds and notifies them.
     * If no committers are found in the next higher level, look one level higher.
     * Repeat if necessary. 
     */
    @SuppressWarnings("unchecked")
	private void notifyUpstreamCommitters(final AbstractBuild<?, ?> build,
			final BuildListener buildListener) {
		boolean committerNotified = false;
		Map<AbstractProject, Integer> upstreamBuilds = build.getUpstreamBuilds();
		
		while (!committerNotified && !upstreamBuilds.isEmpty()) {
			Map<AbstractProject, Integer> currentLevel = upstreamBuilds;
			// new map for the builds one level higher up:
			upstreamBuilds = new HashMap<AbstractProject, Integer>();
			
		    for (Map.Entry<AbstractProject, Integer> entry : currentLevel.entrySet()) {
		        AbstractBuild<?, ?> upstreamBuild = (AbstractBuild<?, ?>) entry.getKey().getBuildByNumber(entry.getValue());
		        Set<User> committers = getCommitters(upstreamBuild);
		        
		        String message = "Attention! Your change in " + upstreamBuild.getProject().getName()
		        + ": " + MessageHelper.getBuildURL(upstreamBuild)
		        + " *might* have broken the downstream job " + build.getProject().getName() + ": " + MessageHelper.getBuildURL(build)
		        + "\nPlease have a look!";
		        
		        for (IMMessageTarget target : calculateIMTargets(committers, buildListener)) {
		            try {
		                log(buildListener, "Sending notification to upstream committer: " + target.toString());
		                getIMConnection().send(target, message);
		                committerNotified = true;
		            } catch (final Throwable e) {
		                log(buildListener, "There was an error sending upstream committer notification to: " + target.toString());
		            }
		        }
		        
		        if (!committerNotified) {
		        	upstreamBuilds.putAll(upstreamBuild.getUpstreamBuilds());
		        }
		    }
		}
	}

    /**
     * Notify all registered chats about the build result.
     */
	private void notifyChats(final AbstractBuild<?, ?> build, final BuildListener buildListener) {
		final StringBuilder sb;
		if (BuildHelper.isFix(build)) {
			sb = new StringBuilder("Yippie, build fixed!\n");
		} else {
			sb = new StringBuilder();
		}
		sb.append("Project ").append(build.getProject().getName())
			.append(" build (").append(build.getNumber()).append("): ")
			.append(BuildHelper.getResultDescription(build)).append(" in ")
			.append(build.getTimestampString())
			.append(": ")
			.append(MessageHelper.getBuildURL(build));
		
		if (! build.getChangeSet().isEmptySet()) {
			boolean hasManyChangeSets = build.getChangeSet().getItems().length > 1;
			for (Entry entry : build.getChangeSet()) {
				sb.append("\n");
				if (hasManyChangeSets) {
					sb.append("* ");
				}
				sb.append(entry.getAuthor()).append(": ").append(entry.getMsg());
			}
		}
		final String msg = sb.toString();

		for (IMMessageTarget target : getNotificationTargets())
		{
		    try {
		        log(buildListener, "Sending notification to: " + target.toString());
		        getIMConnection().send(target, msg);
		    } catch (final Throwable t) {
		        log(buildListener, "There was an error sending notification to: " + target.toString() + "\n" + ExceptionHelper.dump(t));
		    }
		}
	}

	/* (non-Javadoc)
	 * @see hudson.tasks.Publisher#prebuild(hudson.model.Build, hudson.model.BuildListener)
	 */
	@Override
	public boolean prebuild(AbstractBuild<?, ?> build, BuildListener buildListener) {
		try {
			if (notifyOnBuildStart) {
				final StringBuilder sb = new StringBuilder("Starting build ").append(build.getNumber())
					.append(" for job ").append(build.getProject().getName());

				if (build.getPreviousBuild() != null) {
					sb.append(" (previous build: ")
						.append(BuildHelper.getResultDescription(build.getPreviousBuild()));

					if (build.getPreviousBuild().getResult().isWorseThan(Result.SUCCESS)) {
						AbstractBuild<?, ?> lastSuccessfulBuild = build.getPreviousNotFailedBuild();
						if (lastSuccessfulBuild != null) {
							sb.append(" -- last ").append(BuildHelper.getResultDescription(lastSuccessfulBuild))
								.append(" #").append(lastSuccessfulBuild.getNumber())	
								.append(" ").append(lastSuccessfulBuild.getTimestampString()).append(" ago");
						}
					}
					sb.append(")");
				}
				final String msg = sb.toString();
				for (final IMMessageTarget target : getNotificationTargets()) {
					// only notify group chats
					if (target instanceof GroupChatIMMessageTarget) {
		                try {
		                    getIMConnection().send(target, msg);
		                } catch (final Throwable e) {
		                    log(buildListener, "There was an error sending notification to: " + target.toString());
		                }
					}
	            }
			}
		} catch (Throwable t) {
			// ignore: never, ever cancel a build because a notification fails
            log(buildListener, "There was an error in the IM plugin: " + ExceptionHelper.dump(t));
		}
		return true;
	}
	
	private static Set<User> getCommitters(AbstractBuild<?, ?> build) {
		Set<User> committers = new HashSet<User>();
		ChangeLogSet<? extends Entry> changeSet = build.getChangeSet();
		for (Entry entry : changeSet) {
			committers.add(entry.getAuthor());
		}
		return committers;
	}
	
	/**
	 * Returns the culprits WITHOUT the committers to the current build.
	 */
	private static Set<User> getCulpritsOnly(AbstractBuild<?, ?> build) {
		Set<User> culprits = new HashSet<User>(build.getCulprits());
		culprits.removeAll(getCommitters(build));
		return culprits;
	}
	
	private Collection<IMMessageTarget> calculateIMTargets(Set<User> targets, BuildListener listener) {
		Set<IMMessageTarget> suspects = new HashSet<IMMessageTarget>();
		
		String defaultIdSuffix = ((IMPublisherDescriptor)getDescriptor()).getDefaultIdSuffix();
		LOGGER.fine("Default Suffix: " + defaultIdSuffix);
		
		for (User target : targets) {
			LOGGER.fine("Possible target: " + target.getId());
            String imId = getConfiguredIMId(target);
			if (imId == null && defaultIdSuffix != null) {
                imId = target.getId() + defaultIdSuffix;
            }

            if (imId != null) {
                try {
                    suspects.add(CONVERTER.fromString(imId));
                } catch (final IMMessageTargetConversionException e) {
                    log(listener, "Invalid IM ID: " + imId);
                }
            } else {
            	log(listener, "No IM ID found for: " + target.getId());
            }
		}
		return suspects;
	}

    @Override
    public abstract BuildStepDescriptor<Publisher> getDescriptor();
	
    // migrate old JabberPublisher instances
    private Object readResolve() {
    	if (this.strategy == null && this.notificationStrategy != null) {
    		this.strategy = NotificationStrategy.valueOf(this.notificationStrategy.name());
    		this.notificationStrategy = null;
    	}
    	return this;
    }
    
    protected abstract String getConfiguredIMId(User user);
}
