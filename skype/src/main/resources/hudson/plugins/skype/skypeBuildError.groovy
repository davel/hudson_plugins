/*
 * if build error occured that error messsage send to skype
 *
 * @author udagawa
 */
import com.skype.Chat;
import com.skype.Skype;

def class SkypeUtil {
	def sendMessage(String chatName, String message) {
	    Chat[] chats = Skype.getAllChats();
	    for (Chat chat : chats) {
	        String windowTitle = chat.getWindowTitle();
	        // windowTitle : username | title  or title
	        String[] titles = chat.getWindowTitle().split(" \\| ");
	        if (titles.length == 2) {
	            windowTitle = titles[1];
	        }
	        if (chatName.equals(windowTitle)) {
	            chat.send(message);
	            return;
	        }
	    }
	}
}

class LoopBreak extends Throwable {}

def class SkypeBuildErrorPlugin {
	def debug = false;
	def skypeConfig;
	def message = "";
	def lineSeparator = System.getProperty('line.separator')

	def SkypeBuildErrorPlugin(skypeConfig_) {
		skypeConfig = skypeConfig_;
		if (debug) {
			println("skype build error started. build is ${skypeConfig.build}. chat name is ${skypeConfig.chatName}")
		}
	}
	
	def createMessage() {
		def fileName = skypeConfig.build.getRootDir().toString() +  File.separator + "log"
		def LinkedList list = new LinkedList(); // why java does not support simple FIFO class.
		def errorLineSize = skypeConfig.errorLineSize;
		def match = 0;
		try {
			new File(fileName).newReader().eachLine { line ->
				if (list.size() == errorLineSize) {
					list.remove(0);
				}
				list.add(line);

				if (line =~ /${skypeConfig.errorStringPattern}/) {
					match ++;
				}
				if (match > 0) {
					match ++;
				}
				if (match > errorLineSize / 2) {
					throw new LoopBreak(); // Groovy does not support closure break
				}
			}
		} catch (LoopBreak ignore) {
		}
		if (match > 0) {
			message = list.join(lineSeparator);
		}
		return message != ""
	}
	
	def sendMessage() {
		if (debug) {
			println("skype build error end. build is ${skypeConfig.build}. chat name is ${skypeConfig.chatName}. ${message}")
		}
		new SkypeUtil().sendMessage(skypeConfig.chatName, "[${skypeConfig.build.project.name}]Build Error" + lineSeparator + message);
	}
}


def plugin = new SkypeBuildErrorPlugin(skypeConfig);
if (plugin.createMessage()) {
	plugin.sendMessage();
}
