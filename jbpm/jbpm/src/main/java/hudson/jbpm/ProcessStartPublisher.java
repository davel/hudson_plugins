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

import hudson.Launcher;
import hudson.jbpm.model.ProcessInstanceAction;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class ProcessStartPublisher extends Publisher {

	private String processDefinition;
	
	@DataBoundConstructor
	public ProcessStartPublisher(String processDefinition) {
		super();
		this.processDefinition = processDefinition;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {

		JbpmContext context = JbpmConfiguration.getInstance()
				.createJbpmContext();

		try {
			ProcessInstance instance = context.newProcessInstance(processDefinition);
			instance.setKey(build.getParent().getName() + "#"
					+ build.getNumber());

			instance.getContextInstance().setVariable("project",
					build.getProject());
			instance.getContextInstance().setVariable("build", build);

			build.addAction(new ProcessInstanceAction(instance.getId()));

			instance.signal();

			context.save(instance);
		} finally {
			context.close();
		}

		return true;
	}

	public String getProcessDefinition() {
		return processDefinition;
	}

	public Descriptor<Publisher> getDescriptor() {
		return DESCRIPTOR;
	}

	public static Descriptor<Publisher> DESCRIPTOR = new DescriptorImpl();

	public static class DescriptorImpl<Publisher> extends Descriptor {

		@Override
		public Describable newInstance(StaplerRequest req, JSONObject formData)
				throws FormException {
			return req.bindJSON(ProcessStartPublisher.class, formData);
		}

		protected DescriptorImpl() {
			super(ProcessStartPublisher.class);
		}
		
		public List<String> getDefinitions() {
			ArrayList<String> list = new ArrayList<String>();
			for (ProcessDefinition pd: PluginImpl.INSTANCE.getLatestProcessDefinitions()) {
				list.add(pd.getName());
			}
			return list;
		}

		@Override
		public String getDisplayName() {
			return "Start workflow after build";
		}

	}

}
