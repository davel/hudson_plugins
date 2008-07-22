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
package hudson.jbpm.model.gpd;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("bendpoint")
public class BendPoint {
	public int getW1() {
		return w1;
	}

	public void setW1(int w1) {
		this.w1 = w1;
	}

	public int getH1() {
		return h1;
	}

	public void setH1(int h1) {
		this.h1 = h1;
	}

	public int getW2() {
		return w2;
	}

	public void setW2(int w2) {
		this.w2 = w2;
	}

	public int getH2() {
		return h2;
	}

	public void setH2(int h2) {
		this.h2 = h2;
	}

	@XStreamAsAttribute int w1, h1, w2, h2;
}
