/*******************************************************************************
 * Copyright (c) 2009 Thales Corporate Services SAS                             *
 * Author : Gregory Boissinot                                                   *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package com.thalesgroup.hudson.plugins.copyarchiver;

import hudson.Plugin;
import hudson.tasks.Publisher;
import hudson.util.FormFieldValidator;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * @author Gregory BOISSINOT
 */
public class PluginImpl extends Plugin {

	/**
	 * Registers Doxygen publisher.
	 */
	@Override
	public void start() throws Exception {
		Publisher.PUBLISHERS.add(CopyArchiver.DESCRIPTOR);
		super.start();
	}

	public void doDateTimePatternCheck(final StaplerRequest req,
			StaplerResponse rsp) throws IOException, ServletException {
		(new FormFieldValidator(req, rsp, true) {

			public void check() throws IOException, ServletException {

				String pattern = req.getParameter("value");

				if (pattern == null || pattern.trim().length() == 0) {
					error((new StringBuilder()).append(
							"You must provide a pattern value").toString());
				}

				try {
					new SimpleDateFormat(pattern);
				} catch (NullPointerException npe) {
					error((new StringBuilder()).append("Invalid input: ")
							.append(npe.getMessage()).toString());
					return;
				} catch (IllegalArgumentException iae) {
					error((new StringBuilder()).append("Invalid input: ")
							.append(iae.getMessage()).toString());
					return;
				}

				return;

			}
		}).process();
	}

}
