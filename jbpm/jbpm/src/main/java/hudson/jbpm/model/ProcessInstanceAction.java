/* 
 * Copyright 2008 Tom Huybrechts and hudson.dev.java.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.  
 * 
 */
package hudson.jbpm.model;

import hudson.jbpm.PluginImpl;
import hudson.jbpm.model.gpd.GPD;
import hudson.jbpm.model.gpd.Node;
import hudson.jbpm.rendering.ProcessInstanceRenderer;
import hudson.model.Action;
import hudson.model.Run;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.xml.xpath.XPathExpressionException;

import org.dom4j.DocumentException;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Connects a process instance to a build
 * 
 * @author huybrechts
 */
public class ProcessInstanceAction implements Action {

	private final long processInstanceId;
	private transient GPD gpd;

	public ProcessInstanceAction(long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getDisplayName() {
		return "Workflow Process";
	}

	public String getIconFileName() {
		return null;
	}

	public String getUrlName() {
		return "workflow";
	}

	public long getProcessInstanceId() {
		return processInstanceId;
	}

	public ProcessInstance getProcessInstance() {
		return PluginImpl.INSTANCE.getProcessInstance(processInstanceId);
	}

	public synchronized GPD getGPD() {
		if (gpd == null) {
			gpd = GPD.get(getProcessInstance());
		}
		return gpd;
	}

	public List<TaskInstanceWrapper> getOpenTasks() {
		ProcessInstance processInstance = getProcessInstance();
		List<TaskInstanceWrapper> result = new ArrayList<TaskInstanceWrapper>();
		for (TaskInstance ti : PluginImpl.INSTANCE
				.getOpenTasks(processInstance)) {
			TaskInstanceWrapper hti = new TaskInstanceWrapper(ti);
			result.add(hti);
		}
		return result;
	}

	public List<TaskInstanceWrapper> getMyTasks() {
		ProcessInstance processInstance = getProcessInstance();
		List<TaskInstanceWrapper> result = new ArrayList<TaskInstanceWrapper>();
		for (TaskInstance task : PluginImpl.INSTANCE.getPooledTasks()) {
			if (task.getProcessInstance().equals(processInstance)) {
				result.add(new TaskInstanceWrapper(task));
			}
		}
		for (TaskInstance task : PluginImpl.INSTANCE.getUserTasks()) {
			if (task.getProcessInstance().equals(processInstance)) {
				result.add(new TaskInstanceWrapper(task));
			}
		}
		return result;
	}

	public void doImage(StaplerRequest req, StaplerResponse rsp)
			throws IOException, XPathExpressionException, DocumentException {
		ProcessInstance processInstance = getProcessInstance();
		GPD gpd = getGPD();
		ServletOutputStream output = rsp.getOutputStream();
		ProcessInstanceRenderer panel = new ProcessInstanceRenderer(
				processInstance, gpd);
		BufferedImage aimg = new BufferedImage(panel.getWidth(), panel
				.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = aimg.createGraphics();
		panel.paint(g);
		g.dispose();
		ImageIO.write(aimg, "png", output);
		output.flush();
		output.close();
	}

	public List<ImageMapElement> getNodes() {
		ProcessInstance processInstance = getProcessInstance();
		Collection<TaskInstance> taskInstances = processInstance
				.getTaskMgmtInstance().getTaskInstances();
		List<ImageMapElement> result = new ArrayList<ImageMapElement>();
		for (Node node : getGPD().nodes) {
			Run run = null;
			for (TaskInstance taskInstance : taskInstances) {
				if (taskInstance.getTask().getTaskNode().getName().equals(
						node.getName())) {
					
					run = (Run) taskInstance.getVariableLocally("build");
				}
			}

			if (run != null) {
				ImageMapElement ime = new ImageMapElement(run.toString(), run.getUrl(), node.getX(), node.getY(), node.getX()
						+ node.getWidth(), node.getY() + node.getHeight());

				result.add(ime);
			}
		}

		return result;
	}

	public static class ImageMapElement {
		public final int x1, y1, x2, y2;
		public final String name;
		public final String url;

		public ImageMapElement(String name, String url, int x1, int y1, int x2,
				int y2) {
			super();
			this.name = name;
			this.url = url;
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
		}

	}
}
