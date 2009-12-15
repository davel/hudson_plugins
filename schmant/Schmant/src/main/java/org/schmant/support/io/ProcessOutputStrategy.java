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
 * This is an interface implemented by strategy objects for dealing with the
 * output from external processes.
 * <p>
 * Implementations of this interface should be immutable.
 * @author Karl Gustafsson
 * @since 0.5
 */
public interface ProcessOutputStrategy
{
	/**
	 * Create a new process output listener for listening to a process.
	 * @return A new process output listener.
	 */
	ProcessOutputListener getOutputListener();
}
