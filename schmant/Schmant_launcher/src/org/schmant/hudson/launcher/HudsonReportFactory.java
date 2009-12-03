package org.schmant.hudson.launcher;

import hudson.model.BuildListener;

import org.schmant.report.Report;
import org.schmant.report.ReportFactory;
import org.schmant.report.ReportLineFormatter;

final class HudsonReportFactory implements ReportFactory
{
	private final BuildListener m_listener;
	private ReportLineFormatter m_reportLineFormatter;
	
	HudsonReportFactory(BuildListener bl)
	{
		m_listener = bl;
	}
	
	public Report newReport(String identifier)
	{
		return new HudsonReport(identifier, m_reportLineFormatter, m_listener);
	}

	public void setReportLineFormatter(ReportLineFormatter rlf)
	{
		m_reportLineFormatter = rlf;
	}
}
