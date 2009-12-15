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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is an abstract base class that may be inherited by
 * {@link ProcessOutputListener}:s. This class implements the polling mechanism
 * for reading output from a process' stdout or stderr channels (one of them).
 * Subclasses implement a mechanism for storing that output somewhere.
 * <p>
 * If the monitored process generates a lot of output and the polling interval
 * is set too high, the monitored process can freeze because it has filled its
 * output buffer. In that case, set a shorter polling interval.
 * @author Karl Gustafsson
 * @since 0.5
 */
public abstract class AbstractProcessOutputListener implements ProcessOutputListener
{
	/** The default poll interval. */
	public static final long DEFAULT_POLL_INTERVAL = 200L;

	private BufferedReader m_inReader;
	/** The poll interval in milliseconds. */
	private final long m_pollInterval;
	private final AtomicBoolean m_done = new AtomicBoolean(false);

	/**
	 * Create a process output listener with the default polling interval.
	 * {@link #setStream(InputStream)} must be called before using this object.
	 */
	protected AbstractProcessOutputListener()
	{
		m_pollInterval = DEFAULT_POLL_INTERVAL;
	}

	/**
	 * Create a process output listener with the supplied polling interval.
	 * {@link #setStream(InputStream)} must be called before using this object.
	 * @param pollInterval The polling interval in milliseconds.
	 */
	protected AbstractProcessOutputListener(long pollInterval)
	{
		m_pollInterval = pollInterval;
	}

	/**
	 * Must be set before the thread is started.
	 * @param is The stream to listen to.
	 */
	public void setStream(InputStream is)
	{
		m_inReader = new BufferedReader(new InputStreamReader(is));
	}

	/**
	 * Set by the owning thread when this thread should shut down.
	 */
	public void flagDone()
	{
		m_done.set(true);
	}

	/**
	 * Subclasses override this if they want to do any initialization when the
	 * listener thread is starting.
	 * <p>
	 * Long running initialization should probably be performed when the output
	 * listener is created instead since the process to listen to will be
	 * running when this method is called.
	 * <p>
	 * This implementation does nothing.
	 */
	protected void setup()
	{
		// Nothing
	}

	/**
	 * Subclasses implement this to handle one line of output from the monitored
	 * process.
	 * @param l The line.
	 */
	protected abstract void processLine(String l);

	/**
	 * This closes the input stream. Subclasses should make sure to call {@code
	 * super.close()} if overriding this method.
	 * @throws IOException On I/O errors
	 */
	protected void close() throws IOException
	{
		m_inReader.close();
	}

	public void run()
	{
		try
		{
			setup();

			while (true)
			{
				try
				{
					while (m_inReader.ready())
					{
						processLine(m_inReader.readLine());
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				if (m_done.get())
				{
					// Ok, we're done
					return;
				}

				try
				{
					Thread.sleep(m_pollInterval);
				}
				catch (InterruptedException e)
				{
					// Clear interrupted flag
					Thread.interrupted();
					// Die
					return;
				}
			}
		}
		finally
		{
			try
			{
				close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * This may be implemented by subclasses to return the entire output of the
	 * monitored process if they keep it in memory.
	 * @return {@code null}. Override for other behavior.
	 */
	public String getOutput()
	{
		return null;
	}
}
