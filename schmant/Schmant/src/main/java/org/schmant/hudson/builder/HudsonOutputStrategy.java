package org.schmant.hudson.builder;

import java.io.PrintStream;

import org.schmant.support.io.ProcessOutputListener;
import org.schmant.support.io.ProcessOutputStrategy;

final class HudsonOutputStrategy implements ProcessOutputStrategy
{
	private final PrintStream m_logger;

	HudsonOutputStrategy(PrintStream logger)
	{
		m_logger = logger;
	}

	public ProcessOutputListener getOutputListener()
	{
		return new HudsonOutputListener(m_logger);
	}
}
