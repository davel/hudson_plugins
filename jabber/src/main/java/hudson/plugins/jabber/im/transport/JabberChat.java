package hudson.plugins.jabber.im.transport;

import hudson.plugins.jabber.im.IMChat;
import hudson.plugins.jabber.im.IMException;
import hudson.plugins.jabber.im.IMMessageListener;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;

/**
 * 1-on-1 Jabber chat.
 * 
 * @author kutzi
 */
public class JabberChat implements IMChat {
    private Chat chat;

    public JabberChat(Chat chat) {
        this.chat = chat;
    }

    public void sendMessage(String msg) throws IMException {
        try {
            this.chat.sendMessage(msg);
        } catch (XMPPException e) {
            throw new IMException(e);
        }
    }

    public String getNickName(String sender) {
        String s = sender;
        int index = s.indexOf('/');
        if (index != -1) {
            s = s.substring(0, index);
        }

        index = s.indexOf('@');
        if (index != -1) {
            s = s.substring(0, index);
        }
        return s;
    }
    
    public void addMessageListener(IMMessageListener listener) {
        this.chat.addMessageListener(new JabberMessageListenerAdapter(listener));
    }

    public void removeMessageListener(IMMessageListener listener) {
		// doesn't work out-of the box with Smack
    	// We would need to access the underlying connection to remove the packetListener
	}

	public boolean isMultiUserChat() {
        return false;
    }
}
