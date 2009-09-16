package hudson.plugins.im.bot;

import hudson.plugins.im.tools.MessageHelper;

import java.util.Random;

import org.apache.commons.lang.StringUtils;

/**
 * Give the bot a snack!
 * (this is really more to familiarize myself with working with Hudson/jabber
 * @author R. Tyler Ballance <tyler@slide.com>
 */
public class SnackCommand extends AbstractTextSendingCommand {
	
	private static final String HELP = " [<snack>] - om nom nmo";

	private static final String[] THANKS = new String[] {
			"thanks a lot! nom nom nom.",
			"you're so kind to me!",
			"yummy!",
			"great! yum yum." };

	private static final String[] THANKS_WITH_FOOD = new String[] {
			"I really like that %s",
			"how did you know that %s is my favorite food?",
			"I just love %s!",
			"I could eat %s all day long" };

    private final Random ran = new Random();

	@Override
	protected String getReply(String sender, String[] args) {
        String snack = null;
        if (args.length > 1) {
            snack = StringUtils.join(MessageHelper.copyOfRange(args, 1, args.length), " ");
        }

        StringBuilder msg = new StringBuilder(sender).append(": ");
        int index = ran.nextInt(THANKS.length);
        msg.append(THANKS[index]);

        if (snack != null) {
            msg.append(" ");
            index = ran.nextInt(THANKS_WITH_FOOD.length);
            msg.append(String.format(THANKS_WITH_FOOD[index], snack));
        }
		return msg.toString();
	}

	public String getHelp() {
		return HELP;
	}

}
