/*
 * The MIT License
 * 
 * Copyright (c) 2009, Ushus Technologies LTD.,Shinod K Mohandas
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.postbuildtask;

import java.util.Collection;

import hudson.util.Iterators;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * A task properties.
 * 
 * @author Shinod Mohandas
 */
public final class TaskProperties {
	/**
	 * The text string which shoud be searched in the build log.
	 */
	public LogProperties[] logTexts;
	
	public String logText;
	/**
	 * Shell script to be executed.
	 */
	public String script;
	
	{
		if(script != null && !script.trim().equals(""))
			addLogTextToArray();
		
	}

	@DataBoundConstructor
	public TaskProperties(String script) {
		this.script = script;
	}

	public void setLogTexts(LogProperties[] logTexts) {
		this.logTexts = logTexts;
	}

	public LogProperties[] getLogProperties() {
		return logTexts;
	}

	public String getScript() {
		return script;
	}
	
	private void addLogTextToArray() {
		logTexts = new LogProperties[] {new LogProperties(logText,"AND")};
	}

	/*
	 * public TaskProperties(LogProperties[] logTexts, String script) {
	 * this.logTexts = logTexts; this.script = script; }
	 */

	/*
	 * public TaskProperties(Collection<LogProperties> logTexts,String script) {
	 * this((LogProperties[])logTexts.toArray(new
	 * LogProperties[logTexts.size()]),script); }
	 */

	// TODO
	/*
	 * public String getLogText() { return null; }
	 */

}
