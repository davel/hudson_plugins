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

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.config.ReleaseDescriptor;

/**
 * Prepare for a release in SCM.
 * 
 * @aggregator
 * @goal release
 */
public class ReleaseMojo extends AbstractReleaseMojo {

    /**
     * Resume a previous release attempt from the point that it was stopped.
     * 
     * @parameter expression="${resume}" default-value="false"
     */
    private boolean resume;

    /**
     * Whether to generate <code>release-pom.xml</code> files that contain
     * resolved information about the project.
     * 
     * @parameter default-value="false" expression="${generateReleasePoms}"
     */
    private boolean generateReleasePoms;

    /**
     * Whether to use "edit" mode on the SCM, to lock the file for editing
     * during SCM operations.
     * 
     * @parameter expression="${useEditMode}" default-value="false"
     */
    private boolean useEditMode;

    /**
     * Whether to update dependencies version to the next development version.
     * 
     * @parameter expression="${updateDependencies}" default-value="true"
     */
    private boolean updateDependencies;

    /**
     * Whether to use the release profile that adds sources and javadocs to the released artifact, if appropriate.
     *
     * @parameter expression="${useReleaseProfile}" default-value="true"
     */
    private boolean useReleaseProfile;

    /**
     * Dry run: don't checkin or tag anything in the scm repository, or modify
     * the checkout. Running <code>mvn -DdryRun=true release:prepare</code> is
     * useful in order to check that modifications to poms and scm operations
     * (only listed on the console) are working as expected. Modified POMs are
     * written alongside the originals without modifying them.
     * 
     * @parameter expression="${dryRun}" default-value="false"
     */
    private boolean dryRun;

    /**
     * Whether to add a schema to the POM if it was previously missing on
     * release.
     * 
     * @parameter expression="${addSchema}" default-value="true"
     */
    private boolean addSchema;

    /**
     * Goals to run as part of the preparation step, after transformation but
     * before committing. Space delimited.
     * 
     * @parameter expression="${preparationGoals}" default-value="clean verify"
     */
    private String preparationGoals;

    /**
     * 
     * Commits to do are atomic or by project.
     * 
     * @parameter expression="${commitByProject}" default-value="false"
     */
    private boolean commitByProject;

    /**
     * Comma or space separated goals to execute on deployment.
     * 
     * @parameter expression="${goals}"
     */
    private String goals;

    /**
     * Repository that releases are compared against (id::layout::url). Defaults
     * to the deployment repository.
     * 
     * @parameter expression="${baseRepository}"
     */
    private String baseRepository;

    /**
     * @parameter expression="${project.distributionManagementArtifactRepository}"
     * @readonly
     */
    private ArtifactRepository deploymentRepository;

    /**
     * Repository that releases are compared against (id::layout::url)
     * 
     * @parameter expression="${altDeploymentRepository}"
     */
    private String altDeploymentRepository;

    /**
     * Repository that releases are compared against (id::layout::url)
     * 
     * @parameter expression="${tempDeploymentRepository}"
     *            default-value="temprelease::default::file:///${project.build.directory}/repository"
     */
    private String tempDeploymentRepository;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * Base version for releases.
     * 
     * @parameter expression="${version}"
     */
    private String version;

    /**
     * @parameter expression="${project.version}"
     */
    private String projectVersion;
    
    /**
     * @parameter expression="${deploy}" default-value="false"
     */
    private boolean deploy;

    public void execute() throws MojoExecutionException, MojoFailureException {
	super.execute();

	ReleaseDescriptor config = createReleaseDescriptor();
	config.setAddSchema(addSchema);
	config.setGenerateReleasePoms(generateReleasePoms);
	config.setScmUseEditMode(useEditMode);
	config.setPreparationGoals(preparationGoals);
	config.setCommitByProject(commitByProject);
	config.setUpdateDependencies(updateDependencies);
	config.setAutoVersionSubmodules(true);
	config.setCheckoutDirectory(basedir.getAbsolutePath());
	config.setUseReleaseProfile(useReleaseProfile);
	config.setInteractive(false); 
	config.setDeploy(deploy);

	String dr;
	if (altDeploymentRepository != null) {
	    dr = altDeploymentRepository;
	} else {
	    dr = deploymentRepository.getId()
		    + "::"
		    + (deploymentRepository.getLayout() instanceof DefaultRepositoryLayout ? "default"
			    : "legacy") + "::" + deploymentRepository.getUrl();
	}
	config.setDeploymentRepository(dr);

	if (baseRepository != null) {
	    config.setBaseRepository(baseRepository);
	} else {
	    config.setBaseRepository(dr);
	}
	config.setTempDeploymentRepository(tempDeploymentRepository);
	config.setLocalRepository(localRepository.getBasedir());

	if (version != null) {
	    config.setBaseVersion(version);
	} else {
	    config.setBaseVersion(projectVersion.replace("-SNAPSHOT", ".vSVNDATE"));
	}
	String additionalArguments = config.getAdditionalArguments();
	if (additionalArguments != null && additionalArguments.contains("-DaltDeploymentRepository=")) {
	    throw new MojoExecutionException(
		    "Don't specify an altDeploymentRepository!");
	}
	additionalArguments = additionalArguments
		+ " -DaltDeploymentRepository='" + tempDeploymentRepository + "'";
	config.setAdditionalArguments(additionalArguments);

	if (goals == null) {
	    // set default
	    goals = "clean deploy";
	    if (project.getDistributionManagement() != null
		    && project.getDistributionManagement().getSite() != null) {
		goals += " site-deploy";
	    }
	}
	config.setPerformGoals(goals);

	try {
	    releaseManager.stagingRelease(config, settings, reactorProjects,
		    resume, dryRun);
	} catch (ReleaseExecutionException e) {
	    throw new MojoExecutionException(e.getMessage(), e);
	} catch (ReleaseFailureException e) {
	    throw new MojoFailureException(e.getMessage());
	}
    }

}
