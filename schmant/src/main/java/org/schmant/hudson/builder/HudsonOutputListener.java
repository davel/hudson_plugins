package org.schmant.hudson.builder;

import java.io.PrintStream;

import org.schmant.support.io.AbstractProcessOutputListener;

public class HudsonOutputListener extends AbstractProcessOutputListener
{
	private final PrintStream m_logger;

	HudsonOutputListener(PrintStream logger)
	{
		m_logger = logger;
	}

	@Override
	protected void processLine(String l)
	{
		m_logger.println(l);
	}
}
