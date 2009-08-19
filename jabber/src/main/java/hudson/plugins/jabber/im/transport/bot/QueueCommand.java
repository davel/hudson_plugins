/*
 * Created on Apr 22, 2007
 */
package hudson.plugins.jabber.im.transport.bot;

import hudson.model.Hudson;
import hudson.model.Queue;
import hudson.model.Queue.Item;
import hudson.plugins.jabber.im.IMChat;
import hudson.plugins.jabber.im.IMException;
import hudson.plugins.jabber.im.IMMessage;

/**
 * Queue command for the jabber bot.
 * @author Pascal Bleser
 */
public class QueueCommand implements BotCommand {
	
	private static final String HELP = " - show the state of the build queue";

	public void executeCommand(IMChat chat, IMMessage message,
			String sender, String[] args) throws IMException {
		Queue queue = Hudson.getInstance().getQueue();
		Item[] items = queue.getItems();
		String reply;
		if (items.length > 0) {
			StringBuffer msg = new StringBuffer();
			msg.append("Build queue:");
			for (Item item : queue.getItems()) {
				msg.append("\n- ")
				.append(item.task.getName())
				.append(": ").append(item.getWhy());
			}
			reply = msg.toString();
		} else {
			reply = "build queue is empty";
		}
		
		chat.sendMessage(reply);
	}

	public String getHelp() {
		return HELP;
	}

}
