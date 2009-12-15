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

import java.io.InputStream;

/**
 * This defines a {@code Runnable} class that regularly polls an {@code
 * InputStream} for output.
 * <p>
 * A process output listener object is run in its own thread. When it is done
 * listening, it returns from the {@code run} method.
 * @author Karl Gustafsson
 * @since 0.5
 */
public interface ProcessOutputListener extends Runnable
{
	/**
	 * Set the stream to listen to.
	 * @param is The stream.
	 */
	void setStream(InputStream is);

	/**
	 * This is called by the owner when the listener should stop listening to
	 * the stream and return from the {@code run} method.
	 */
	void flagDone();

	/**
	 * Get the entire output from the monitored process. This is optional to
	 * support. It's OK to return {@code null}.
	 * @return The process' output, or {@code null}.
	 */
	String getOutput();
}
