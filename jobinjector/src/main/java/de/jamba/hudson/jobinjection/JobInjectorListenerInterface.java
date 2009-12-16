package de.jamba.hudson.jobinjection;

/**
 * The JobInjector listener shall inform us about job run completion.
 */
public interface JobInjectorListenerInterface {
	/**
	 * This method shall be called on job completion.
	 */
	void wakeMeUp();
}
