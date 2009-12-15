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

import java.io.File;
import java.util.List;

/**
 * Configuration settings for running an external process.
 * @author Karl Gustafsson
 * @since 0.5
 */
public class ProcessSettings
{
	private ProcessOutputStrategy m_stdoutStrategy;
	private ProcessOutputStrategy m_stderrStrategy;
	private ArgumentList m_argumentList;
	private List<String> m_environmentVariables;
	private boolean m_inheritEnvironmentVariables = true;
	private File m_workingDirectory;

	/**
	 * Set the strategy to use for collecting the process' stdout output.
	 * @param pos The strategy.
	 * @return {@code this}.
	 */
	public ProcessSettings setStdoutStrategy(ProcessOutputStrategy pos)
	{
		m_stdoutStrategy = pos;
		return this;
	}

	/**
	 * Get the strategy to use for collecting the process' stdout output.
	 * @return The strategy.
	 */
	public ProcessOutputStrategy getStdoutStrategy()
	{
		return m_stdoutStrategy;
	}

	/**
	 * Set the strategy to use for collecting the process' stderr output.
	 * @param pos The strategy.
	 * @return {@code this}.
	 */
	public ProcessSettings setStderrStrategy(ProcessOutputStrategy pos)
	{
		m_stderrStrategy = pos;
		return this;
	}

	/**
	 * Get the strategy to use for collecting the process' stderr output.
	 * @return The strategy.
	 */
	public ProcessOutputStrategy getStderrStrategy()
	{
		return m_stderrStrategy;
	}

	/**
	 * Set the command to use for running the process and the arguments to the
	 * process. See {@link ProcessBuilder}.
	 * @param al The argument list.
	 * @return {@code this}
	 */
	public ProcessSettings setArgumentList(ArgumentList al)
	{
		m_argumentList = al;
		return this;
	}

	/**
	 * Get the command and its arguments.
	 * @return The command and its arguments.
	 */
	public ArgumentList getArgumentList()
	{
		return m_argumentList;
	}

	/**
	 * Set a list of environment variables to set for the process. Each entry in
	 * the list has the format <i>name=value</i>.
	 * @see #setInheritEnvironmentVariables(boolean)
	 * @param l A list of environment variables.
	 * @return {@code this}.
	 */
	public ProcessSettings setEnvironmentVariables(List<String> l)
	{
		m_environmentVariables = l;
		return this;
	}

	/**
	 * Get the list of environment variables to set for the process. Each entry
	 * in the list has the format <i>name=value</i>.
	 * @see #isInheritEnvironmentVariables()
	 * @return The list of environment variables.
	 */
	public List<String> getEnvironmentVariables()
	{
		return m_environmentVariables;
	}

	/**
	 * Should environment variables be inherited from this process? The default
	 * value of this is {@code true}.
	 * <p>
	 * If this is set to {@code false}, the launched process will only see the
	 * environment variables set to {@link #setEnvironmentVariables(List)}.
	 * @see #setEnvironmentVariables(List)
	 * @param b Should environment variables be inherited?
	 * @return {@code this}
	 */
	public ProcessSettings setInheritEnvironmentVariables(boolean b)
	{
		m_inheritEnvironmentVariables = b;
		return this;
	}

	/**
	 * Should environment variables be inherited from the current process?
	 * @return {@code true} if environment variables should be inherited.
	 */
	public boolean isInheritEnvironmentVariables()
	{
		return m_inheritEnvironmentVariables;
	}

	/**
	 * Set the working directory for the process. The default value of this is
	 * the current process' working directory.
	 * @param d A working directory.
	 * @return {@code this}
	 */
	public ProcessSettings setWorkingDirectory(File d)
	{
		m_workingDirectory = d;
		return this;
	}

	/**
	 * Get the working directory for the process.
	 * @return The working directory for the process.
	 */
	public File getWorkingDirectory()
	{
		return m_workingDirectory;
	}

	@Override
	public String toString()
	{
		return m_argumentList + " with environment variables " + (m_environmentVariables != null ? m_environmentVariables : "<none>") + " ("
				+ (!m_inheritEnvironmentVariables ? "don't inherit from current process" : "plus environment variables inherited from current process") + ") and working dir "
				+ (m_workingDirectory != null ? m_workingDirectory.toString() : "<current directory>");
	}
}
