package org.schmant.hudson;

import hudson.Plugin;
import hudson.tasks.BuildStep;

import org.schmant.hudson.builder.SchmantBuilder;

public class SchmantPlugin extends Plugin
{
	public void start() throws Exception
	{
		BuildStep.BUILDERS.add(SchmantBuilder.DESCRIPTOR);
	}
}
