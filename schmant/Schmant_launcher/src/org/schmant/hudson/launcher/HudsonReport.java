package org.schmant.hudson.launcher;

import hudson.model.BuildListener;

import java.io.PrintStream;
import java.util.logging.Level;

import org.schmant.report.AbstractPrintingReport;
import org.schmant.report.ReportLineFormatter;

final class HudsonReport extends AbstractPrintingReport
{
	private final BuildListener m_listener;
	private final PrintStream m_logger;
	
	protected HudsonReport(String identifier, ReportLineFormatter rlf, BuildListener bl)
	{
		super(identifier, rlf);
		m_listener = bl;
		m_logger = bl.getLogger();
	}

	@Override
	protected void printLine(Level l, String s)
	{
		if (l == Level.SEVERE)
		{
			m_listener.fatalError(s);
		}
		else
		{
			m_logger.println(s);
		}
	}

	@Override
	protected void printStacktrace(Level l, String s)
	{
		if (l == Level.SEVERE)
		{
			m_listener.fatalError(s);
		}
		else
		{
			m_logger.println(s);
		}
	}
}
