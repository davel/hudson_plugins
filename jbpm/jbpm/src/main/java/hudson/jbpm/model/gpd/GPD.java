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

import java.io.ByteArrayInputStream;
import java.util.List;

import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("root-container")
public class GPD {

	private static XStream xstream = new XStream();

	static {
		xstream.setClassLoader(GPD.class.getClassLoader());
		xstream.processAnnotations(new Class[] { GPD.class, Node.class,
				Edge.class, Label.class, BendPoint.class });
	}

	public static GPD get(ProcessInstance instance) {
		ProcessDefinition def = instance.getProcessDefinition();
		byte[] gpd = def.getFileDefinition().getBytes("gpd.xml");
		return (GPD) xstream.fromXML(new ByteArrayInputStream(gpd));
	}
	
	
	@XStreamAsAttribute
	public String name;
	@XStreamAsAttribute
	public int width, height;

	@XStreamImplicit(itemFieldName = "node")
	public List<Node> nodes;

	public Node getNode(String name) {
		for (Node node : nodes) {
			if (name.equals(node.name)) {
				return node;
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
