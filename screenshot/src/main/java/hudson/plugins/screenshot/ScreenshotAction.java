package hudson.plugins.screenshot;

import hudson.model.Action;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class ScreenshotAction implements Action {

	private VirtualChannel channel;

	public ScreenshotAction(VirtualChannel channel) {
		this.channel = channel;
	}

	public String getDisplayName() {
		return "Screenshot";
	}

	public String getIconFileName() {
		return null;
	}

	public String getUrlName() {
		return "screenshot";
	}

	public void doIndex(StaplerRequest request, StaplerResponse rsp)
			throws Exception {
		byte[] bytes = channel.call(new CreateScreenshot());
		ServletOutputStream sos = rsp.getOutputStream();
		rsp.setContentType("image/png");
		rsp.setContentLength(bytes.length);
		sos.write(bytes);
		sos.flush();
		sos.close();
	}

	public static class CreateScreenshot implements Callable<byte[], Exception> {

		private static final long serialVersionUID = 1L;

		public byte[] call() throws HeadlessException, AWTException,
				IOException {
			Robot robot = new Robot();
			final Dimension size = Toolkit.getDefaultToolkit().getScreenSize()
					.getSize();
			BufferedImage image = robot
					.createScreenCapture(new Rectangle(size));
			ByteArrayOutputStream os = new ByteArrayOutputStream((int) (size
					.getWidth()
					* size.getHeight() * 4));
			ImageIO.write(image, "png", os);
			byte[] result = os.toByteArray();
			return result;
		}

	}

}
