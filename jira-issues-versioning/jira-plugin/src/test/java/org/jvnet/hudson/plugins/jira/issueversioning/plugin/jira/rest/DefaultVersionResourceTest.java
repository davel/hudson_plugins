package org.jvnet.hudson.plugins.jira.issueversioning.plugin.jira.rest;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Set;

import com.google.common.collect.Sets;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.plugins.server.resourcefactory.SingletonResource;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.test.EmbeddedContainer;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hudson.plugins.jira.issueversioning.domain.api.model.rest.Build;
import org.jvnet.hudson.plugins.jira.issueversioning.domain.api.model.rest.Project;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link DefaultVersionResource}
 *
 * @author Stig Kleppe-Jorgensen, 2009.12.29
 */
public class DefaultVersionResourceTest {
	private DefaultVersionAssociationCreator versionAssociationCreator;

	@Before
	public void startContainer() throws Exception {
		versionAssociationCreator = createMock();

	 	Dispatcher dispatcher = EmbeddedContainer.start().getDispatcher();
		dispatcher.getRegistry().addSingletonResource(new DefaultVersionResource(versionAssociationCreator));
	}

	@After
	public void stopContainer() throws Exception {
		EmbeddedContainer.stop();
	}

	@Test
	public void should_return_ok_with_correct_url() throws Exception {
		Project project = createProject();
		final DefaultVersionResource versionResource = TestPortProvider.createProxy(DefaultVersionResource.class);
		Response response = versionResource.associateWithIssues(project);

		Mockito.verify(versionAssociationCreator).associateFor(project);
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
	}

	@Test
	public void should_return_404_with_url_pointing_to_nonexisting_resource() throws Exception {
		Project project = createProject();
		final DefaultVersionResource versionResource =
				TestPortProvider.createProxy(DefaultVersionResource.class, "/should_get_404");
		Response response = versionResource.associateWithIssues(project);

		assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
	}

	@Test
	@Ignore
	public void should_post_project_with_builds() throws URISyntaxException, JAXBException {
		final Project project = createProject();
		final byte[] projectBytes = bytesForProjectInstance(project);
		MockHttpRequest request = createRequest(projectBytes);
		MockHttpResponse response = new MockHttpResponse();

		final DefaultVersionAssociationCreator associationCreator = versionAssociationCreator;
		final DefaultVersionResource resource = new DefaultVersionResource(associationCreator);
		createDispatcher(resource).invoke(request, response);

		Mockito.verify(associationCreator).associateFor(project);

		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
		assertEquals("basic", response.getContentAsString());
	}

	private DefaultVersionAssociationCreator createMock() {
		return Mockito.mock(DefaultVersionAssociationCreator.class);
	}

	private byte[] bytesForProjectInstance(final Project project) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(Project.class);
		final Marshaller marshaller = jaxbContext.createMarshaller();

		final StringWriter writer = new StringWriter();
		marshaller.marshal(project, writer);

		return writer.toString().getBytes();
	}

	private MockHttpRequest createRequest(byte[] projectBytes) throws URISyntaxException {
		MockHttpRequest request = MockHttpRequest.post("/version/associate");
		request.contentType(MediaType.APPLICATION_XML_TYPE);
		request.content(projectBytes);

		return request;
	}

	private Dispatcher createDispatcher(final DefaultVersionResource versionResource) {
		Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();
		ResourceFactory noDefaults = new SingletonResource(versionResource);
		dispatcher.getRegistry().addResourceFactory(noDefaults);

		return dispatcher;
	}

	private Project createProject() {
		return new Project("name", "1.2.5", createBuild());
	}

	private Build createBuild() {
		return new Build(12, createIssues());
	}

	private Set<String> createIssues() {
		return Sets.newHashSet("DEV-1234", "DEV-231", "DEV-1432");
	}
}
