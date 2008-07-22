package hudson.plugins.stagingrelease;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.repository.layout.LegacyRepositoryLayout;
import org.apache.maven.plugin.MojoFailureException;

public class RepositoryUtil {

	private static final Pattern ALT_REPO_SYNTAX_PATTERN = Pattern
			.compile("(.+)::(.+)::(.+)");

	public static DefaultArtifactRepository createRepository(String repositoryId)
			throws MojoFailureException {
		Matcher matcher = ALT_REPO_SYNTAX_PATTERN.matcher(repositoryId);

		if (!matcher.matches()) {
			throw new MojoFailureException(repositoryId,
					"Invalid syntax for repository.",
					"Invalid syntax for alternative repository. Use \"id::layout::url\".");
		} else {
			String id = matcher.group(1).trim();
			String layout = matcher.group(2).trim();
			String url = matcher.group(3).trim();

			ArtifactRepositoryLayout repoLayout;
			if (layout.equals("default")) {
				repoLayout = new DefaultRepositoryLayout();
			} else if (layout.equals("legacy")) {
				repoLayout = new LegacyRepositoryLayout();
			} else {
				throw new MojoFailureException(layout,
						"Unknown repository layout",
						"Unknown repository layout. Use default or legacy");
			}

			return new DefaultArtifactRepository(id, url, repoLayout, true);
		}
	}

}
