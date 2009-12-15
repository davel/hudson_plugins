/* Schmant, the build tool, http://www.schmant.org
 * Copyright (C) 2007-2009 Karl Gustafsson
 *
 * This file is a part of Schmant. 
 *
 * Schmant is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Schmant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.schmant.support.io;

/**
 * An object of this class represents the result from a running process. It
 * contains the process' exit code and optionally its output to stdout and
 * stderr if the used {@link ProcessOutputStrategy} supports that.
 * @author Karl Gustafsson
 * @since 0.5
 */
public class ProcessResult
{
	private final int m_exitCode;
	private final String m_stdoutOutput;
	private final String m_stderrOutput;

	public ProcessResult(int exitCode, String stdout, String stderr)
	{
		m_exitCode = exitCode;
		m_stdoutOutput = stdout;
		m_stderrOutput = stderr;
	}

	/**
	 * Get the exit code from the process.
	 * @return The process' exit code.
	 */
	public int getExitCode()
	{
		return m_exitCode;
	}

	/**
	 * Get the process' output to stdout, if that was collected by the
	 * {@link ProcessOutputStrategy} used.
	 * @return The process' output to stdout, or {@code null} if that is not
	 * supported by the {@link ProcessOutputStrategy}.
	 */
	public String getStdoutOutput()
	{
		return m_stdoutOutput;
	}

	/**
	 * Get the process' output to stderr, if that was collected by the
	 * {@link ProcessOutputStrategy} used.
	 * @return The process' output to stderr, or {@code null} if that is not
	 * supported by the {@link ProcessOutputStrategy}.
	 */
	public String getStderrOutput()
	{
		return m_stderrOutput;
	}

	@Override
	public String toString()
	{
		return "External process result: exit code=" + m_exitCode + ", stdout output=[" + m_stdoutOutput + "], stderr output=[" + m_stderrOutput + "]";
	}

}
