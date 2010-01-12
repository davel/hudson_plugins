package hudson.plugins.sonar;

import hudson.UDPBroadcastThread;
import hudson.maven.MavenModuleSet;
import hudson.model.*;
import hudson.plugins.sonar.model.LightProjectConfig;
import hudson.scm.NullSCM;
import hudson.tasks.Maven;
import hudson.util.jna.GNUCLibrary;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.SingleFileSCM;

import java.io.File;

/**
 * @author Evgeny Mandrikov
 */
public abstract class SonarTestCase extends HudsonTestCase {
  /**
   * Setting this to non-existent host, allows us to avoid intersection with exist Sonar.
   */
  public static final String SONAR_HOST = "http://example.org:9999/sonar";
  public static final String DATABASE_PASSWORD = "password";

  public static final String ROOT_POM = "sonar-pom.xml";
  public static final String SONAR_INSTALLATION_NAME = "default";

  /**
   * We should override default port defined in {@link hudson.UDPBroadcastThread#PORT}
   * to avoid intersections with real Hudson on Nemo.
   */
  public static final Integer UdpBroadcastPort = 33849;

  /**
   * @throws Exception if something is wrong
   */
  @Override
  protected void setUp() throws Exception {
    System.setProperty("hudson.udp", UdpBroadcastPort.toString());
    if (UDPBroadcastThread.PORT != 33849) {
      throw new RuntimeException("UdpBroadcastPort");
    }
    super.setUp();
  }

  /**
   * Returns Fake Maven Installation.
   *
   * @return Fake Maven Installation
   * @throws Exception if something is wrong
   */
  @Override
  protected Maven.MavenInstallation configureDefaultMaven() throws Exception {
    File mvn = new File(getClass().getResource("SonarTestCase/maven/bin/mvn").toURI().getPath());
    if (!Hudson.isWindows()) {
      //noinspection OctalInteger
      GNUCLibrary.LIBC.chmod(mvn.getPath(), 0755);
    }
    String home = mvn.getParentFile().getParentFile().getAbsolutePath();
    Maven.MavenInstallation mavenInstallation = new Maven.MavenInstallation("default", home, NO_PROPERTIES);
    hudson.getDescriptorByType(Maven.DescriptorImpl.class).setInstallations(mavenInstallation);
    return mavenInstallation;
  }

  protected SonarInstallation configureDefaultSonar() {
    return configureSonar(new SonarInstallation(SONAR_INSTALLATION_NAME));
  }

  protected SonarInstallation configureSonar(SonarInstallation sonarInstallation) {
    hudson.getDescriptorByType(SonarPublisher.DescriptorImpl.class).setInstallations(sonarInstallation);
    return sonarInstallation;
  }

  protected MavenModuleSet setupMavenProject() throws Exception {
    return setupMavenProject("pom.xml");
  }

  protected MavenModuleSet setupMavenProject(String pomName) throws Exception {
    MavenModuleSet project = super.createMavenProject("MavenProject");
    // Setup SCM
    project.setScm(new SingleFileSCM(pomName, getClass().getResource("/hudson/plugins/sonar/SonarTestCase/pom.xml")));
    // Setup Maven
    project.setRootPOM(pomName);
    project.setGoals("clean install");
    project.setIsArchivingDisabled(true);
    // Setup Sonar
    project.getPublishersList().add(newSonarPublisherForMavenProject());
    return project;
  }

  protected FreeStyleProject setupFreeStyleProject() throws Exception {
    return setupFreeStyleProject(ROOT_POM);
  }

  protected FreeStyleProject setupFreeStyleProject(String pomName) throws Exception {
    FreeStyleProject project = super.createFreeStyleProject("FreeStyleProject");
    // Setup SCM
    project.setScm(new NullSCM());
    // Setup Sonar
    project.getPublishersList().add(newSonarPublisherForFreeStyleProject(pomName));
    return project;
  }

  protected AbstractBuild<?, ?> build(AbstractProject<?, ?> project) throws Exception {
    return build(project, null);
  }

  protected AbstractBuild<?, ?> build(AbstractProject<?, ?> project, Result expectedStatus) throws Exception {
    return build(project, new Cause.RemoteCause("", ""), expectedStatus);
  }

  protected AbstractBuild<?, ?> build(AbstractProject<?, ?> project, Cause cause, Result expectedStatus) throws Exception {
    AbstractBuild<?, ?> build = project.scheduleBuild2(0, cause).get();
    if (expectedStatus != null) {
      assertBuildStatus(expectedStatus, build);
    }
    return build;
  }

  protected static SonarPublisher newSonarPublisherForMavenProject() {
    return new SonarPublisher(SONAR_INSTALLATION_NAME, null, null);
  }

  protected static final LightProjectConfig PROJECT_CONFIG = new LightProjectConfig(
      "test", "test",
      "Test", // TODO can be ${JOB_NAME} ?
      "0.1-SNAPSHOT", // Version
      "Test project", // Description,
      null,
      "src",
      "UTF-8",
      null,
      null
  );

  protected static SonarPublisher newSonarPublisherForFreeStyleProject(String pomName) {
    return new SonarPublisher(
        SONAR_INSTALLATION_NAME,
        null,
        null,
        null,
        "default", // Maven Installation Name
        pomName, // Root POM
        PROJECT_CONFIG
    );
  }

  /**
   * Asserts that Sonar executed with given arguments.
   *
   * @param build build
   * @param args  command line arguments
   * @throws Exception if something is wrong
   */
  protected void assertSonarExecution(AbstractBuild build, String args) throws Exception {
    // Check command line arguments
    assertLogContains(args + " -e -B sonar:sonar", build);

    // Check that Sonar Plugin started
//    assertLogContains("[INFO] Sonar host: " + SONAR_HOST, build);

    // SONARPLUGINS-320: Check that small badge was added to build history
    assertNotNull(
        BuildSonarAction.class.getSimpleName() + " not found",
        build.getAction(BuildSonarAction.class)
    );

    // SONARPLUGINS-165: Check that link added to project
    // FIXME Godin: I don't know why, but this don't work for FreeStyleProject
    // AbstractProject project = build.getProject();
    // assertNotNull(project.getAction(ProjectSonarAction.class));
  }

  protected void assertNoSonarExecution(AbstractBuild build, String cause) throws Exception {
    assertLogContains(cause, build);
    // SONARPLUGINS-320: Check that small badge was not added to build history
    assertNull(
        BuildSonarAction.class.getSimpleName() + " found",
        build.getAction(BuildSonarAction.class)
    );
  }
}
