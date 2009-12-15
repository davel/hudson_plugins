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

import java.io.IOException;

/**
 * Support class for running external processes. This is used by the external
 * process task.
 * <p>
 * Hudson plugin note: This class is borrowed from Schmant and somewhat
 * de-schmantified.
 * @author Karl Gustafsson
 * @since 0.5
 */
public final class ProcessSupport
{
	/**
	 * Hidden constructor
	 */
	private ProcessSupport()
	{
		// Nothing
	}

	/**
	 * Execute the command and wait for it to complete.
	 * @param ps The settings.
	 * @return A {@link ProcessResult} object.
	 * @throws InterruptedException If the execution is interrupted.
	 */
	public static ProcessResult execAndWait(ProcessSettings ps) throws IOException, InterruptedException
	{
		// Start threads that will listen on output on stdout and stderr.
		ProcessOutputListener outListener = ps.getStdoutStrategy().getOutputListener();
		ProcessOutputListener errListener = ps.getStderrStrategy().getOutputListener();
		ProcessBuilder pb = new ProcessBuilder(ps.getArgumentList().getArgumentList());
		if (ps.getWorkingDirectory() != null)
		{
			pb.directory(ps.getWorkingDirectory());
		}
		if (!ps.isInheritEnvironmentVariables())
		{
			pb.environment().clear();
		}
		if (ps.getEnvironmentVariables() != null)
		{
			for (String envVar : ps.getEnvironmentVariables())
			{
				int equalPos = envVar.indexOf('=');
				if (equalPos < 0)
				{
					throw new RuntimeException("Invalid format of environment variable declaration: " + envVar);
				}
				pb.environment().put(envVar.substring(0, equalPos).trim(), envVar.substring(equalPos + 1).trim());
			}
		}
		Process p;
		p = pb.start();
		outListener.setStream(p.getInputStream());
		Thread olt = new Thread(outListener);
		olt.start();
		try
		{
			errListener.setStream(p.getErrorStream());
			Thread elt = new Thread(errListener);
			elt.start();
			try
			{
				p.waitFor();
			}
			catch (InterruptedException e)
			{
				p.destroy();
				throw e;
			}
			finally
			{
				// A maybe softer way of stopping the thread than interrupting
				// it
				errListener.flagDone();
				elt.join();
			}
		}
		finally
		{
			// A maybe softer way of stopping the thread than interrupting it
			outListener.flagDone();
			olt.join();
		}

		return new ProcessResult(p.exitValue(), outListener.getOutput(), errListener.getOutput());
	}
}
