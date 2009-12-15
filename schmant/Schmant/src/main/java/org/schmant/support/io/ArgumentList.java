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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Objects of this class is used for building an command and argument list that
 * is used for running an external command. The {@link #add(java.lang.String)}
 * method is responsible for quoting and escaping the contents of the string as
 * necessary.
 * @author Karl Gustafsson
 * @since 0.5
 */
public class ArgumentList
{
	private static final Pattern BACKSLASH_PATTERN = Pattern.compile("\\", Pattern.LITERAL);

	private final List<String> m_argumentList = new ArrayList<String>();
	// Should strings containing spaces be quoted?
	private final boolean m_quoteStringsWSpaces;

	/**
	 * Create the argument list.
	 * @param quoteStringsWSpaces Should an added string containing spaces be
	 * quoted?
	 */
	public ArgumentList(boolean quoteStringsWSpaces)
	{
		m_quoteStringsWSpaces = quoteStringsWSpaces;
	}

	/**
	 * Check if the argument is already quoted. Declared package private so that
	 * it can be unit tested.
	 * @param arg The argument to check
	 * @return {@code true} if already quoted
	 */
	boolean isAlreadyQuoted(String arg)
	{
		return ((arg.startsWith("\"") && arg.endsWith("\"")) || (arg.startsWith("'") && arg.endsWith("'")));
	}

	/**
	 * Add one argument to the list. If there are characters that need to be
	 * escaped in the argument, this method takes care of that.
	 * @param arg The argument. {@code null} arguments are allowed.
	 * @return {@code this}
	 */
	public ArgumentList add(String arg)
	{
		if (arg != null)
		{
			// This must be used instead of String.replaceAll since the
			// replacement string contains back slashes.
			arg = BACKSLASH_PATTERN.matcher(arg).replaceAll(Matcher.quoteReplacement("\\\\"));
			if (m_quoteStringsWSpaces && arg.contains(" ") && !isAlreadyQuoted(arg))
			{
				arg = "\"" + arg + "\"";
			}
		}
		m_argumentList.add(arg);
		return this;
	}

	/**
	 * Add a collection of arguments to the list. The arguments are added in the
	 * order that they are returned when iterating over the collection. If there
	 * are characters that need to be escaped in the arguments, this method
	 * takes care of that.
	 * @param c The argument collection.
	 * @return {@code this}
	 */
	public ArgumentList addAll(Collection<String> c)
	{
		for (String s : c)
		{
			add(s);
		}
		return this;
	}

	/**
	 * Add all arguments in the supplied argument list. The arguments are
	 * assumed to be processed already, and are added as-is.
	 * @param al The argument list to add arguments from.
	 * @return {@code this}
	 */
	public ArgumentList addAll(ArgumentList al)
	{
		for (String s : al.getArgumentList())
		{
			m_argumentList.add(s);
		}
		return this;
	}

	/**
	 * Get the argument list.
	 * @return The argument list.
	 */
	public List<String> getArgumentList()
	{
		return m_argumentList;
	}

	@Override
	public String toString()
	{
		StringBuilder res = new StringBuilder();
		boolean first = true;
		for (String arg : m_argumentList)
		{
			if (!first)
			{
				res.append(' ');
			}
			res.append(arg);
			first = false;
		}
		return res.toString();
	}
}
