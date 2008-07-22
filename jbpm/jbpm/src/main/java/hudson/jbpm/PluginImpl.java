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
package hudson.jbpm;

import hudson.Plugin;
import hudson.jbpm.model.TaskInstanceWrapper;
import hudson.jbpm.model.UserTasks;
import hudson.jbpm.rendering.GraphicsUtil;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.TopLevelItem;
import hudson.tasks.Publisher;
import hudson.triggers.Trigger;
import hudson.util.PluginServletFilter;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import org.acegisecurity.Authentication;
import org.acegisecurity.userdetails.UserDetails;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.db.TaskMgmtSession;
import org.jbpm.file.def.FileDefinition;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class PluginImpl extends Plugin {

	private static Logger log = Logger.getLogger("hudson.jbpm");

	public static PluginImpl INSTANCE;
	private ProcessDefinition processDefinition;

	public ProcessDefinition getProcessDefinition() {
		return processDefinition;
	}

	private HudsonTaskListener taskListener;

	public PluginImpl() {
	}

	@Override
	public void start() throws Exception {
		JbpmConfiguration.getInstance().startJobExecutor();
		PluginServletFilter.addFilter(new JbpmContextFilter());
		new HudsonRunListener().register();
		Publisher.PUBLISHERS.add(ProcessStartPublisher.DESCRIPTOR);
		Trigger.timer.schedule(taskListener = new HudsonTaskListener(), 10000, 10000);
		Hudson.getInstance().getWidgets().add(new UserTasks());
		GraphicsUtil.class.getName(); // trigger icon initialization
		INSTANCE = this;
	}

	public List<ProcessDefinition> getProcessDefinitions() {
		JbpmContext context = getCurrentJbpmContext();
		return context.getGraphSession().findAllProcessDefinitions();
	}

	public List<ProcessDefinition> getLatestProcessDefinitions() {
		JbpmContext context = getCurrentJbpmContext();
		return context.getGraphSession().findLatestProcessDefinitions();
	}

	public ProcessInstance getProcessInstance(long processInstanceId) {
		JbpmContext context = getCurrentJbpmContext();
		return context.getGraphSession().getProcessInstance(processInstanceId);
	}

	public List<ProcessInstance> getProcessInstances(
			ProcessDefinition definition) {
		JbpmContext context = getCurrentJbpmContext();
		return context.getGraphSession().findProcessInstances(
				definition.getId());
	}

	public TaskInstanceWrapper getTaskInstance(String taskInstanceId) {
		long l = Long.parseLong(taskInstanceId);
		return new TaskInstanceWrapper(getCurrentJbpmContext().getTaskInstance(
				l));
	}

	private static JbpmContext getCurrentJbpmContext() {
		return JbpmConfiguration.getInstance().getCurrentJbpmContext();
	}

	public List<TaskInstance> getOpenTasks(ProcessInstance processInstance) {
		JbpmContext context = getCurrentJbpmContext();
		List<TaskInstance> result = context.getTaskMgmtSession()
				.findTaskInstancesByProcessInstance(processInstance);
		return result;
	}

	public List<TaskInstance> getPooledTasks() {
		JbpmContext context = getCurrentJbpmContext();
		TaskMgmtSession taskMgmtSession = context.getTaskMgmtSession();

		if (!Hudson.getAuthentication().isAuthenticated()) {
			return Collections.emptyList();
		}

		List<String> projectNames = new ArrayList<String>();
		for (TopLevelItem item : Hudson.getInstance().getItems()) {
			if ((item instanceof Job)
					&& ((Job) item).hasPermission(Job.CONFIGURE)) {
				projectNames.add(item.getName());
			}
		}

		if (projectNames.isEmpty()) {
			return Collections.emptyList();
		}

		List<TaskInstance> result = taskMgmtSession
				.findPooledTaskInstances(projectNames);

		return result;
	}

	/**
	 * Draws a JPEG for a process definition
	 * 
	 * @param req
	 * @param rsp
	 * @param processDefinition
	 * @throws IOException
	 */
	public void doProcessDefinitionImage(StaplerRequest req,
			StaplerResponse rsp,
			@QueryParameter("processDefinition") long processDefinition)
			throws IOException {

		JbpmContext context = getCurrentJbpmContext();
		ProcessDefinition definition = context.getGraphSession()
				.getProcessDefinition(processDefinition);
		FileDefinition fd = definition.getFileDefinition();
		byte[] bytes = fd.getBytes("processimage.jpg");
		rsp.setContentType("image/jpeg");
		ServletOutputStream output = rsp.getOutputStream();

		BufferedImage loaded = ImageIO.read(new ByteArrayInputStream(bytes));
		BufferedImage aimg = new BufferedImage(loaded.getWidth(), loaded
				.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = aimg.createGraphics();
		g.drawImage(loaded, null, 0, 0);
		g.dispose();
		ImageIO.write(aimg, "jpg", output);
		output.flush();
		output.close();
	}

	/**
	 * Method supporting upload from the designer at /plugin/jbpm/upload
	 */
	public void doUpload(StaplerRequest req, StaplerResponse rsp)
			throws FileUploadException, IOException, ServletException {
		try {
			ServletFileUpload upload = new ServletFileUpload(
					new DiskFileItemFactory());

			// Parse the request
			FileItem fileItem = (FileItem) upload.parseRequest(req).get(0);

			if (fileItem.getContentType().indexOf("application/x-zip-compressed") == -1) {
				throw new IOException("Not a process archive");
			}

			log.fine("Deploying process archive " + fileItem.getName());
			ZipInputStream zipInputStream = new ZipInputStream(fileItem
					.getInputStream());
			JbpmContext jbpmContext = getCurrentJbpmContext();
			log.fine("Preparing to parse process archive");
			ProcessDefinition processDefinition = ProcessDefinition
					.parseParZipInputStream(zipInputStream);
			log
					.fine("Created a processdefinition : "
							+ processDefinition.getName());
			jbpmContext.deployProcessDefinition(processDefinition);
			zipInputStream.close();
			rsp.forwardToPreviousPage(req);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void stop() throws Exception {
		JbpmConfiguration.getInstance().getJobExecutor().stop();
		taskListener.cancel();
	}

	public List<TaskInstance> getUserTasks() {
		Authentication authentication = Hudson.getInstance().getAuthentication();
		if ("anonymous".equals(authentication.getPrincipal())) {
			return Collections.emptyList();
		}
		JbpmContext context = getCurrentJbpmContext();
		String userName = ((UserDetails) authentication
				.getPrincipal()).getUsername();
		return context.getTaskList(userName);
	}
	
	public static void injectTransientVariables(ContextInstance contextInstance) {
		contextInstance.setTransientVariable("hudson", Hudson.getInstance());
	}

}
