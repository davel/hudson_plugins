package hudson.plugins.im.bot;

import hudson.plugins.im.IMChat;
import hudson.plugins.im.IMException;
import hudson.plugins.im.IMMessage;

import java.util.logging.Logger;

/**
 * Abstract command for sending a reply back to the sender.
 * 
 * @author kutzi
 */
public abstract class AbstractTextSendingCommand implements BotCommand {
	
	private static final Logger LOGGER = Logger.getLogger(AbstractTextSendingCommand.class.getName());

	/**
	 * {@inheritDoc}
	 */
	public final void executeCommand(IMChat chat, IMMessage message,
			String sender, String[] args) throws IMException {
		String reply;
		try {
			reply = getReply(sender, args);
		} catch (RuntimeException e) {
			LOGGER.warning(e.toString());
			reply = sender + ": Error " + e.toString();
		}
		chat.sendMessage(reply);
	}

	/**
	 * Gets the text reply
	 * 
	 * @param sender the room nickname of the command sender
	 * @param args arguments passed to the command, where <code>args[0]</code> is the command name itself
	 * @throws RuntimeException in case of invalid args. This is automatically caught and reported to the sender
	 */
	protected abstract String getReply(String sender, String args[]);

}
