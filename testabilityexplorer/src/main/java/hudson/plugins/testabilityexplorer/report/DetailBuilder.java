package hudson.plugins.testabilityexplorer.report;

import hudson.model.AbstractBuild;

import java.util.Collection;

/**
 * Builds Objects that are used in report details.
 *
 * @author reik.schatz
 */
public interface DetailBuilder<T>
{
    Object buildDetail(final String link, final String originalRequestUri, final AbstractBuild<?, ?> build, final Collection<T> results);
}
