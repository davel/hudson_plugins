/**
 * 
 */
package hudson.plugins.ircbot.v2;

import hudson.plugins.im.IMConnectionListener;
import hudson.plugins.im.IMMessage;
import hudson.plugins.im.IMMessageListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import org.jibble.pircbot.PircBot;

public class PircConnection extends PircBot {

	private static final Logger LOGGER = Logger.getLogger(PircConnection.class.getName());
	
	private final List<IMConnectionListener> listeners = new CopyOnWriteArrayList<IMConnectionListener>();
	
	private final List<MessageListener> msgListeners = new CopyOnWriteArrayList<MessageListener>();
	
	private final List<JoinListener> joinListeners = new CopyOnWriteArrayList<JoinListener>();
	
	private volatile boolean explicitDisconnect = false;

	public PircConnection(String name) {
        setName(name);
        //setMessageDelay(5);
    }

	public void sendIMMessage(String target, String message) {
		// many IRC clients don't seem to handle new lines well (see e.g. https://bugzilla.redhat.com/show_bug.cgi?id=136542)
		// Therefore the following won't work most of the time:
//		message = message.replace("\n", "\020n");
//		sendNotice(target, message);
		
		// send multiple messages instead: 
		
		String[] lines = message.split("\\r?\\n|\\r");
		for (String line : lines) {
			sendNotice(target, line);
		}
	}
	
    /**
     * {@inheritDoc} 
     */
    @Override
    protected void onMessage(String channel, String sender,
            String login, String hostname, String message) {
    	for (MessageListener l : this.msgListeners) {
    		if(l.target.equals(channel)) {
    			l.listener.onMessage(new IMMessage(sender, channel, message));
    		}
    	}
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    protected void onPrivateMessage(String sender, String login,
            String hostname, String message) {
    	for (MessageListener l : this.msgListeners) {
    		if (getName().equals(l.target)) {
    			l.listener.onMessage(new IMMessage(sender, getNick(), message));
    		}
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
    	for (JoinListener l : this.joinListeners) {
    		if (getName().equals(sender)) {
    			l.channelJoined(channel);
    		}
    	}
    }
    
    @Override
    protected void onServerResponse(int code, String response) {
    	if (code >= 400 && code <= 599) {
    		LOGGER.warning("IRC server responded error " + code + " Message:\n" +
    				response);
    	}
    }
    
    public final void closeConnection() {
    	this.explicitDisconnect = true;
    	super.disconnect();
    }
    
    @Override
	protected void onDisconnect() {
    	if (!explicitDisconnect) {
	    	for (IMConnectionListener l : this.listeners) {
	    		l.connectionBroken(null);
	    	}
    	}
    	explicitDisconnect = false;
		super.onDisconnect();
	}

    // Note that the add/removeXyzListener methods needn't be synchronized because of the CopyOnWriteLists
    
	public void addConnectionListener(IMConnectionListener listener) {
    	this.listeners.add(listener);
    }
    
    public void removeConnectionListener(IMConnectionListener listener) {
    	this.listeners.remove(listener);
    }

	public void addMessageListener(String target, IMMessageListener listener) {
		this.msgListeners.add(new MessageListener(target, listener));
	}

	public void removeMessageListener(String target, IMMessageListener listener) {
		this.msgListeners.remove(new MessageListener(target, listener));
	}
	
	public void addJoinListener(JoinListener listener) {
		this.joinListeners.add(listener);
	}

	public void removeJoinListener(JoinListener listener) {
		this.joinListeners.remove(listener);
	}
	
	private static final class MessageListener {
		private final String target;
		private final IMMessageListener listener;

		public MessageListener(String expectedMessageTarget, IMMessageListener listener) {
			this.target = expectedMessageTarget;
			this.listener = listener;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((listener == null) ? 0 : listener.hashCode());
			result = prime * result
					+ ((target == null) ? 0 : target.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MessageListener other = (MessageListener) obj;
			if (listener == null) {
				if (other.listener != null)
					return false;
			} else if (!listener.equals(other.listener))
				return false;
			if (target == null) {
				if (other.target != null)
					return false;
			} else if (!target.equals(other.target))
				return false;
			return true;
		}
	}
	
	public interface JoinListener {
		void channelJoined(String channelName);
	}
}