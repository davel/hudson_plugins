package hudson.plugins.stagingrelease;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.config.ReleaseDescriptor;

/**
 * Sets the version for the project and all submodules.
 * 
 * @aggregator
 * @goal set-version
 */
public class SetVersionMojo extends AbstractReleaseMojo {

	/**
	 * Whether to add a schema to the POM if it was previously missing on
	 * release.
	 * 
	 * @parameter expression="${addSchema}" default-value="true"
	 */
	private boolean addSchema;

	/**
	 * Base version for releases.
	 * 
	 * @parameter expression="${version}"
	 * @required
	 */
	private String version;

	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();

		ReleaseDescriptor config = createReleaseDescriptor();
		config.setAddSchema(addSchema);
		config.setUpdateDependencies(true);
		config.setAutoVersionSubmodules(true);
		config.setCheckoutDirectory(basedir.getAbsolutePath());
		config.setInteractive(false); // override !
		config.setBaseVersion(version);

		try {
			releaseManager.setVersion(config, settings, reactorProjects, null);
		} catch (ReleaseExecutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (ReleaseFailureException e) {
			throw new MojoFailureException(e.getMessage());
		}
	}

}
