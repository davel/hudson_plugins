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

import java.util.List;

import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.ReleaseManagerListener;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;

/**
 * Release management classes.
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public interface StagingReleaseManager {
	/**
	 * The Plexus role.
	 */
	String ROLE = StagingReleaseManager.class.getName();

	/**
	 * Do a staging release
	 * 
	 * @param releaseDescriptor
	 *            the configuration to pass to the preparation steps
	 * @param settings
	 *            the settings.xml configuration
	 * @param reactorProjects
	 *            the reactor projects
	 * @param resume
	 *            resume a previous release, if the properties file exists
	 * @param dryRun
	 *            do not commit any changes to the file system or SCM
	 * @throws ReleaseExecutionException
	 *             if there is a problem performing the release
	 * @throws ReleaseFailureException
	 *             if there is a problem performing the release
	 */
	void stagingRelease(ReleaseDescriptor releaseDescriptor, Settings settings,
			List<MavenProject> reactorProjects, boolean resume, boolean dryRun)
			throws ReleaseExecutionException, ReleaseFailureException;

	void setVersion(ReleaseDescriptor releaseDescriptor, Settings settings,
			List<MavenProject> reactorProjects, ReleaseManagerListener listener)
			throws ReleaseExecutionException, ReleaseFailureException;

}
