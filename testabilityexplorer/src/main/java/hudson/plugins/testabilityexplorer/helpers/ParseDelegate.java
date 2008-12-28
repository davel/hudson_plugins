package hudson.plugins.testabilityexplorer.helpers;

import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.FilePath;

import java.io.Serializable;

/**
 * Processes report files and add's actions to the {@link BuildProxy} based on the outcome.
 *
 * @author reik.schatz
 */
public interface ParseDelegate extends Serializable
{
    boolean perform(BuildProxy build, BuildListener listener);
}
