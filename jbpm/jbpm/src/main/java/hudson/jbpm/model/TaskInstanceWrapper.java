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

import hudson.jbpm.ProcessClassLoaderCache;
import hudson.model.Action;
import hudson.model.Hudson;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletException;

import org.acegisecurity.userdetails.UserDetails;
import org.apache.commons.lang.StringUtils;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * This task records that a build is triggered by a JBPM task instance
 * 
 * @author huybrechts
 * 
 */
public class TaskInstanceWrapper implements Action {

	private final long taskInstanceId;
	private TaskInstance taskInstance;

	public TaskInstanceWrapper(long taskInstanceId) {
		this.taskInstanceId = taskInstanceId;
	}

	public TaskInstanceWrapper(TaskInstance ti) {
		this.taskInstance = ti;
		taskInstanceId = taskInstance.getId();
	}

	public long getId() {
		return taskInstanceId;
	}

	public String getDisplayName() {
		return null;
	}

	public String getIconFileName() {
		return null;
	}

	public String getUrlName() {
		return null;
	}

	public synchronized TaskInstance getTaskInstance() {
		if (taskInstance == null) {
			JbpmContext context = JbpmConfiguration.getInstance()
					.getCurrentJbpmContext();
			taskInstance = context.getTaskMgmtSession().getTaskInstance(
					taskInstanceId);
		}
		return taskInstance;
	}

	public Object getForm() {
		TaskInstance ti = getTaskInstance();
		try {
			ClassLoader processClassLoader = ProcessClassLoaderCache.INSTANCE
					.getClassLoader(ti.getProcessInstance()
							.getProcessDefinition());
			String formClass = (String) ti.getVariableLocally("form");
			if (formClass == null) {
				return new Form(ti);
			} else {
				Class<?> cl = processClassLoader.loadClass(formClass);
				return cl.getConstructor(TaskInstance.class).newInstance(ti);
			}
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public void doTriggerTransition(StaplerRequest req, StaplerResponse rsp,
			@QueryParameter("transition") String transition)
			throws ServletException, IOException {
		JbpmContext context = JbpmConfiguration.getInstance()
				.getCurrentJbpmContext();
		TaskInstance taskInstance = getTaskInstance();
		if (StringUtils.isEmpty(transition)) {
			taskInstance.end();
		} else {
			taskInstance.end(transition);
		}
		context.save(taskInstance);
		rsp.forwardToPreviousPage(req);
	}

	public void doStart(StaplerRequest req, StaplerResponse rsp)
			throws ServletException, IOException {
		String userName = ((UserDetails) Hudson.getInstance().getAuthentication().getPrincipal()).getUsername();
		JbpmContext context = JbpmConfiguration.getInstance()
				.getCurrentJbpmContext();
		TaskInstance taskInstance = getTaskInstance();
		taskInstance.setActorId(userName);
		taskInstance.start();
		context.save(taskInstance);
		rsp.forwardToPreviousPage(req);
	}
}
