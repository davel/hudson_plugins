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

import hudson.PluginManager;
import hudson.model.Hudson;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jbpm.file.def.FileDefinition;
import org.jbpm.graph.def.ProcessDefinition;

public class ProcessClassLoaderCache {

	public static final ProcessClassLoaderCache INSTANCE = new ProcessClassLoaderCache();

	private Map<Long, ClassLoader> cache = new HashMap<Long, ClassLoader>();

	private File cacheRoot = new File(Hudson.getInstance().getRootDir(),
			"jbpm/ProcessDefinitionCache");

	private ProcessClassLoaderCache() {
	}

	public synchronized ClassLoader getClassLoader(ProcessDefinition def) throws IOException {
		ClassLoader cl = cache.get(def.getId());
		if (cl == null) {
			File pdCache = new File(cacheRoot, Long.toString(def.getId()));
			if (!pdCache.exists()) {
				FileDefinition fd = def.getFileDefinition();
				for (Map.Entry<String, byte[]> entry : ((Map<String, byte[]>) fd
						.getBytesMap()).entrySet()) {
					File f = new File(pdCache, entry.getKey());
					f.getParentFile().mkdirs();
					FileOutputStream fos = new FileOutputStream(f);
					IOUtils.copy(new ByteArrayInputStream(entry.getValue()),
							fos);
					fos.close();
				}
			}
			cl = new URLClassLoader(new URL[] { new URL(
					pdCache.toURI().toURL(), "classes/") }, Hudson.getInstance().getPluginManager().uberClassLoader) {

						@Override
						public Class<?> loadClass(String name)
								throws ClassNotFoundException {
							System.out.println(name);
							return super.loadClass(name);
						}
				
			};
			cache.put(def.getId(), cl);
		}
		return cl;
	}

}
