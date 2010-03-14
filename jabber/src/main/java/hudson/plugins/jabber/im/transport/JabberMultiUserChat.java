package hudson.plugins.jabber.im.transport;

import hudson.plugins.im.IMChat;
import hudson.plugins.im.IMException;
import hudson.plugins.im.IMMessageListener;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

public class JabberMultiUserChat implements IMChat {
    
    private final MultiUserChat chat;
	private final JabberIMConnection connection;

    public JabberMultiUserChat (MultiUserChat chat, JabberIMConnection connection) {
         this.chat = chat;
         this.connection = connection;
     }

    public void sendMessage(String msg) throws IMException {
        try {
            this.chat.sendMessage(msg);
        } catch (XMPPException e) {
            throw new IMException(e);
        }
    }

    public String getNickName(String sender) {
    	// Jabber has the chosen MUC nickname in the resource part of the sender id
    	String resource = JabberUtil.getResourcePart(sender);
        if (resource != null) {
            return resource;
        }
        return sender;
    }
    
    public void addMessageListener(IMMessageListener listener) {
        this.chat.addMessageListener(
        		new JabberMUCMessageListenerAdapter(listener, this.connection, this.chat));
    }

    public void removeMessageListener(IMMessageListener listener) {
    	// doesn't work out-of the box with Smack
    	// We would need to access the underlying connection to remove the packetListener
	}

	public boolean isMultiUserChat() {
        return true;
    }
}
