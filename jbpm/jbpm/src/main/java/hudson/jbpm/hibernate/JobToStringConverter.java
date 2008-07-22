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
package hudson.jbpm.hibernate;

import hudson.model.Hudson;
import hudson.model.Job;

import org.jbpm.context.exe.Converter;

public class JobToStringConverter implements Converter {

	private static final long serialVersionUID = 1L;

	public boolean supports(Object value) {
		if (value == null)
			return true;
		return Job.class.isAssignableFrom(value.getClass());
	}

	public Object convert(Object o) {
		Job job = (Job) o;
		String convertedValue = job.getName();
		return convertedValue;
	}

	public Object revert(Object o) {
		String name = (String) o;
		return Hudson.getInstance().getItem(name);
	}
}
