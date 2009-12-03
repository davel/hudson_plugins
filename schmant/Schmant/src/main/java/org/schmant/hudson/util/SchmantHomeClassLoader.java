package org.schmant.hudson.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

/**
 * Class loader for loading the Schmant launcher classes and the classes from a
 * Schmant installation's lib directory.
 * @author Karl Gustafsson
 * @since 1.0
 */
public final class SchmantHomeClassLoader extends ClassLoader implements Closeable
{
	private final Map<String, byte[]> m_launcherClasses;
	private final List<JarFile> m_jarFiles;

	private static final class JarFileFilter implements FileFilter
	{
		private static final JarFileFilter INSTANCE = new JarFileFilter();

		public boolean accept(File pathname)
		{
			return pathname.isFile() && pathname.getName().endsWith(".jar");
		}
	}

	/**
	 * @param additionalClasspath A path separator-separated string containing
	 * absolute paths to Jar files.
	 */
	public SchmantHomeClassLoader(ClassLoader parent, File schmantHome, String additionalClasspath, InputStream launcherJar) throws IOException
	{
		super(parent);
		File libDir = new File(schmantHome, "lib");
		if (!libDir.exists())
		{
			throw new IOException("Invalid Schmant home " + schmantHome + ": " + schmantHome + File.separator + "lib does not exist");
		}
		else if (!libDir.isDirectory())
		{
			throw new IOException("Invalid Schmant home " + schmantHome + ": " + schmantHome + File.separator + "lib is not a directory");
		}

		// Add files from Schmant's lib directory to the classpath
		File[] files = libDir.listFiles(JarFileFilter.INSTANCE);
		m_jarFiles = new ArrayList<JarFile>();
		for (File f : files)
		{
			m_jarFiles.add(new JarFile(f));
		}
		
		// Add the additional Jar files to the classpath
		if (additionalClasspath != null)
		{
			String[] entries = additionalClasspath.split("\\Q" + File.pathSeparator + "\\E");
			for (String entry : entries)
			{
				File f = new File(entry);
				if (!f.exists())
				{
					System.err.println("The file " + entry + " on the additional class path does not exist. It will be ignorded.");
				}
				else if (!f.isFile())
				{
					System.err.println(entry + " on the additional class path is not a file. It will be ignorded.");
				}
				else
				{
					m_jarFiles.add(new JarFile(f));
				}
			}
		}

		m_launcherClasses = new HashMap<String, byte[]>();
		JarInputStream jis = new JarInputStream(launcherJar);
		JarEntry je = jis.getNextJarEntry();
		while (je != null)
		{
			String name = je.getName();
			if (name.endsWith(".class"))
			{
				int classDataLen = (int) je.getSize();
				byte[] classData = new byte[classDataLen];
				int noRead = jis.read(classData);
				if (noRead != classDataLen)
				{
					throw new IOException("Wanted to read " + classDataLen + " for class " + name + ". Got " + noRead);
				}
				m_launcherClasses.put(name.substring(0, name.length() - 6).replace('/', '.'), classData);
			}
			je = jis.getNextJarEntry();
		}
	}

	private Class<?> loadClassFromJar(String name, JarFile jf, ZipEntry ze) throws ClassNotFoundException
	{
		try
		{
			InputStream is = jf.getInputStream(ze);
			try
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] barr = new byte[4096];
				int noRead = is.read(barr);
				while (noRead > 0)
				{
					baos.write(barr, 0, noRead);
					noRead = is.read(barr);
				}
				barr = baos.toByteArray();
				return defineClass(name, baos.toByteArray(), 0, barr.length);
			}
			finally
			{
				is.close();
			}
		}
		catch (IOException e)
		{
			throw new ClassNotFoundException("When loading class from " + jf, e);
		}
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException
	{
		byte[] classData = m_launcherClasses.get(name);
		if (classData != null)
		{
			return defineClass(name, classData, 0, classData.length);
		}

		String className = name.replace('.', '/') + ".class";
		for (JarFile jf : m_jarFiles)
		{
			ZipEntry ze = jf.getEntry(className);
			if (ze != null)
			{
				return loadClassFromJar(name, jf, ze);
			}
		}
		throw new ClassNotFoundException(name);
	}

	@Override
	public InputStream getResourceAsStream(String name)
	{
		InputStream res = super.getResourceAsStream(name);
		if (res != null)
		{
			// Found in parent class loader
			return res;
		}

		// The ClassLoader JavaDocs does not specify how to handle leading
		// slashes. Be nice about it.
		while (name.charAt(0) == '/')
		{
			name = name.substring(1);
		}

		for (JarFile jf : m_jarFiles)
		{
			ZipEntry ze = jf.getEntry(name);
			if (ze != null)
			{
				try
				{
					return jf.getInputStream(ze);
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		return null;
	}
	
	public void close() throws IOException
	{
		// Close all Jar files
		for (JarFile jf : m_jarFiles)
		{
			jf.close();
		}
		m_jarFiles.clear();
		m_launcherClasses.clear();
	}
}
