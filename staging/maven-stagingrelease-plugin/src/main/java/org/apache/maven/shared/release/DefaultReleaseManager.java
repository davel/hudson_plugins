package org.apache.maven.shared.release;

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

import hudson.plugins.stagingrelease.StagingReleaseManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.config.ReleaseDescriptorStore;
import org.apache.maven.shared.release.config.ReleaseDescriptorStoreException;
import org.apache.maven.shared.release.phase.ReleasePhase;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

/**
 * Implementation of the release manager.
 * 
 */
public class DefaultReleaseManager extends AbstractLogEnabled implements
		StagingReleaseManager {
	/**
	 * The phases of release to run, and in what order.
	 */
	private List<String> stagingReleasePhases;

	private List<String> setVersionPhases;

	/**
	 * The available phases.
	 */
	private Map<String, ReleasePhase> releasePhases;

	/**
	 * The configuration storage.
	 */
	private ReleaseDescriptorStore configStore;

	private static final int PHASE_SKIP = 0, PHASE_START = 1, PHASE_END = 2,
			GOAL_START = 11, GOAL_END = 12, ERROR = 99;

	public void stagingRelease(ReleaseDescriptor releaseDescriptor,
			Settings settings, List<MavenProject> reactorProjects,
			boolean resume, boolean dryRun) throws ReleaseExecutionException,
			ReleaseFailureException {
		stagingRelease(releaseDescriptor, settings, reactorProjects, resume,
				dryRun, null, null);
	}

	private void stagingRelease(ReleaseDescriptor releaseDescriptor,
			Settings settings, List<MavenProject> reactorProjects,
			boolean resume, boolean dryRun, ReleaseManagerListener listener,
			ReleaseResult result) throws ReleaseExecutionException,
			ReleaseFailureException {
		updateListener(listener, "stagingRelease", GOAL_START);

		ReleaseDescriptor config;
		if (resume) {
			config = loadReleaseDescriptor(releaseDescriptor, listener);
		} else {
			config = releaseDescriptor;
		}

		// Later, it would be a good idea to introduce a proper workflow tool so
		// that the release can be made up of a
		// more flexible set of steps.

		String completedPhase = config.getCompletedPhase();
		int index = stagingReleasePhases.indexOf(completedPhase);

		for (int idx = 0; idx <= index; idx++) {
			updateListener(listener, stagingReleasePhases.get(idx).toString(),
					PHASE_SKIP);
		}

		if (index == stagingReleasePhases.size() - 1) {
			logInfo(
					result,
					"Release preparation already completed. You can now continue with release:perform, "
							+ "or start again using the -Dresume=false flag");
		} else if (index >= 0) {
			logInfo(result, "Resuming release from phase '"
					+ stagingReleasePhases.get(index + 1) + "'");
		}

		// start from next phase
		for (int i = index + 1; i < stagingReleasePhases.size(); i++) {
			String name = (String) stagingReleasePhases.get(i);

			ReleasePhase phase = (ReleasePhase) releasePhases.get(name);

			if (phase == null) {
				throw new ReleaseExecutionException("Unable to find phase '"
						+ name + "' to execute");
			}

			updateListener(listener, name, PHASE_START);

			ReleaseResult phaseResult = null;
			try {
				if (dryRun) {
					phaseResult = phase.simulate(config, settings,
							reactorProjects);
				} else {
					phaseResult = phase.execute(config, settings,
							reactorProjects);
				}
			} finally {
				if (result != null && phaseResult != null) {
					result.getOutputBuffer().append(phaseResult.getOutput());
				}
			}

			config.setCompletedPhase(name);
			try {
				configStore.write(config);
			} catch (ReleaseDescriptorStoreException e) {
				// TODO: rollback?
				throw new ReleaseExecutionException(
						"Error writing release properties after completing phase",
						e);
			}

			updateListener(listener, name, PHASE_END);
		}

		// call release:clean so that resume will not be possible anymore after
		// a perform
		clean(releaseDescriptor, listener, reactorProjects);

		updateListener(listener, "stagingRelease", GOAL_END);
	}

	/**
	 * Determines the path of the working directory. By default, this is the
	 * checkout directory. For some SCMs, the project root directory is not the
	 * checkout directory itself, but a SCM-specific subdirectory.
	 * 
	 * @param checkoutDirectory
	 *            The checkout directory as java.io.File
	 * @param relativePathProjectDirectory
	 *            The relative path of the project directory within the checkout
	 *            directory or ""
	 * @return The working directory
	 */
	protected File determineWorkingDirectory(File checkoutDirectory,
			String relativePathProjectDirectory) {
		if (StringUtils.isNotEmpty(relativePathProjectDirectory)) {
			return new File(checkoutDirectory, relativePathProjectDirectory);
		} else {
			return checkoutDirectory;
		}
	}

	private ReleaseDescriptor loadReleaseDescriptor(
			ReleaseDescriptor releaseDescriptor, ReleaseManagerListener listener)
			throws ReleaseExecutionException {
		try {
			updateListener(listener, "verify-release-configuration",
					PHASE_START);
			ReleaseDescriptor descriptor = configStore.read(releaseDescriptor);
			updateListener(listener, "verify-release-configuration", PHASE_END);
			return descriptor;
		} catch (ReleaseDescriptorStoreException e) {
			updateListener(listener, e.getMessage(), ERROR);

			throw new ReleaseExecutionException(
					"Error reading stored configuration: " + e.getMessage(), e);
		}
	}

	public void clean(ReleaseDescriptor releaseDescriptor,
			ReleaseManagerListener listener, List<MavenProject> reactorProjects) {
		updateListener(listener, "cleanup", PHASE_START);

		getLogger().info("Cleaning up after release...");

		configStore.delete(releaseDescriptor);

		for (Iterator<String> i = stagingReleasePhases.iterator(); i.hasNext();) {
			String name = i.next();

			ReleasePhase phase = releasePhases.get(name);

			phase.clean(reactorProjects);
		}

		updateListener(listener, "cleanup", PHASE_END);
	}

	void setConfigStore(ReleaseDescriptorStore configStore) {
		this.configStore = configStore;
	}

	void updateListener(ReleaseManagerListener listener, String name, int state) {
		if (listener != null) {
			switch (state) {
			case GOAL_START:
				listener.goalStart(name, getGoalPhases(name));
				break;
			case GOAL_END:
				listener.goalEnd();
				break;
			case PHASE_SKIP:
				listener.phaseSkip(name);
				break;
			case PHASE_START:
				listener.phaseStart(name);
				break;
			case PHASE_END:
				listener.phaseEnd();
				break;
			default:
				listener.error(name);
			}
		}
	}

	private List<String> getGoalPhases(String name) {
		List<String> phases = new ArrayList<String>();

		if ("stagingRelease".equals(name)) {
			phases.addAll(this.stagingReleasePhases);
		}

		return Collections.unmodifiableList(phases);
	}

	private void logInfo(ReleaseResult result, String message) {
		if (result != null) {
			result.appendInfo(message);
		}

		getLogger().info(message);
	}

	private void captureException(ReleaseResult result,
			ReleaseManagerListener listener, Exception e) {
		updateListener(listener, e.getMessage(), ERROR);

		result.appendError(e);

		result.setResultCode(ReleaseResult.ERROR);
	}

	public void setVersion(ReleaseDescriptor releaseDescriptor,
			Settings settings, List<MavenProject> reactorProjects,
			ReleaseManagerListener listener) {
		ReleaseResult result = new ReleaseResult();

		result.setStartTime(System.currentTimeMillis());

		try {
			setVersion(releaseDescriptor, settings, reactorProjects, listener,
					result);

			result.setResultCode(ReleaseResult.SUCCESS);
		} catch (ReleaseExecutionException e) {
			captureException(result, listener, e);
		} catch (ReleaseFailureException e) {
			captureException(result, listener, e);
		} finally {
			result.setEndTime(System.currentTimeMillis());
		}
	}

	private void setVersion(ReleaseDescriptor releaseDescriptor,
			Settings settings, List<MavenProject> reactorProjects,
			ReleaseManagerListener listener, ReleaseResult result)
			throws ReleaseExecutionException, ReleaseFailureException {
		updateListener(listener, "stagingRelease", GOAL_START);

		ReleaseDescriptor config = releaseDescriptor;

		// start from next phase
		for (int i = 0; i < setVersionPhases.size(); i++) {
			String name = (String) setVersionPhases.get(i);

			ReleasePhase phase = (ReleasePhase) releasePhases.get(name);

			if (phase == null) {
				throw new ReleaseExecutionException("Unable to find phase '"
						+ name + "' to execute");
			}

			updateListener(listener, name, PHASE_START);

			ReleaseResult phaseResult = null;
			try {
				phaseResult = phase.execute(config, settings, reactorProjects);
			} finally {
				if (result != null && phaseResult != null) {
					result.getOutputBuffer().append(phaseResult.getOutput());
				}
			}

			config.setCompletedPhase(name);

			updateListener(listener, name, PHASE_END);
		}

		// call release:clean so that resume will not be possible anymore after
		// a perform
		clean(releaseDescriptor, listener, reactorProjects);

		updateListener(listener, "stagingRelease", GOAL_END);
	}

}
