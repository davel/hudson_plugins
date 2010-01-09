package com.ikokoon.serenity.process;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.objectweb.asm.ClassVisitor;

import com.ikokoon.serenity.Configuration;
import com.ikokoon.serenity.instrumentation.VisitorFactory;
import com.ikokoon.toolkit.Toolkit;

/**
 * This class looks through the classpath and collects metrics on the classes that were not instanciated by the classloader during the unit tests and
 * creates a visitor chain for the class that will collect the complexity and dependency metrics for the class.
 * 
 * @author Michael Couck
 * @since 24.07.09
 * @version 01.00
 */
public class Accumulator extends AProcess {

	/** The set of jars that are processed so we don't do the same jar more than once. */
	private Set<String> jarsProcessed = new TreeSet<String>();
	/** The set of classes that are processed so we don't process the files more than once. */
	private Set<String> filesProcessed = new TreeSet<String>();
	/** The chain of adapters for analysing the classes. */
	private Class<ClassVisitor>[] CLASS_ADAPTER_CLASSES;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Accumulator(IProcess parent) {
		super(parent);
		CLASS_ADAPTER_CLASSES = Configuration.getConfiguration().classAdapters.toArray(new Class[Configuration.getConfiguration().classAdapters
				.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public void execute() {
		super.execute();
		String classpath = Configuration.getConfiguration().getClassPath();
		StringTokenizer stringTokenizer = new StringTokenizer(classpath, File.pathSeparator);
		while (stringTokenizer.hasMoreTokens()) {
			String token = stringTokenizer.nextToken();
			File file = new File(token);
			logger.warn("Processing jar : " + token + ", file : " + file.getAbsolutePath());
			if (!file.exists() || !file.canRead()) {
				logger.warn("Can't read file : " + file.getAbsolutePath());
				continue;
			}
			if (file.isFile()) {
				if (token.endsWith(".jar") || token.endsWith(".zip") || token.endsWith(".war") || token.endsWith(".ear")) {
					logger.debug("Processing jar : " + file.getAbsolutePath());
					processJar(file);
				}
			} else if (file.isDirectory()) {
				processDir(file);
			}
		}
	}

	/**
	 * Processes a directory on a file system, looks for class files and feeds the byte code into the adapter chain for collecting the metrics for the
	 * class.
	 * 
	 * @param file
	 *            the directory or file to look in for the class data
	 */
	void processDir(File file) {
		// Iteratively go through the directories
		if (file == null || !file.exists() || !file.canWrite()) {
			return;
		}
		if (file.isDirectory()) {
			File files[] = file.listFiles();
			for (int j = 0; j < files.length; j++) {
				file = files[j];
				processDir(file);
			}
		} else if (file.isFile() && file.canRead()) {
			String filePath = file.getAbsolutePath();
			filePath = Toolkit.slashToDot(filePath);
			if (excluded(filePath)) {
				return;
			}
			byte[] classBytes = Toolkit.getContents(file).toByteArray();
			ByteArrayOutputStream source = new ByteArrayOutputStream();

			String className = null;

			// Strip the beginning of the path off the name
			for (String packageName : Configuration.getConfiguration().includedPackages) {
				if (filePath.indexOf(packageName) > -1) {
					int indexOfPackageName = filePath.indexOf(packageName);
					int classIndex = filePath.lastIndexOf(".class");
					try {
						if (classIndex > -1) {
							className = filePath.substring(indexOfPackageName, classIndex);
							break;
						}
					} catch (Exception e) {
						logger.error("Exception reading the class files in a directory", e);
					}
				}
			}
			if (!filesProcessed.add(className)) {
				return;
			}
			processClass(className, classBytes, source);
		}
	}

	/**
	 * Processes a jar or zip file or something like it, looks for class and feeds the byte code into the adapter chain for collecting the metrics for
	 * the class.
	 * 
	 * @param file
	 *            the file to look in for the class data
	 */
	private void processJar(File file) {
		// Don't process the jars more than once
		if (!jarsProcessed.add(file.getName())) {
			return;
		}

		JarFile jarFile = null;
		try {
			jarFile = new JarFile(file);
		} catch (Exception e) {
			logger.error("Exeption accessing the jar : " + file, e);
			return;
		}
		Enumeration<JarEntry> jarEntries = jarFile.entries();
		while (jarEntries.hasMoreElements()) {
			JarEntry jarEntry = jarEntries.nextElement();
			String entryName = jarEntry.getName();
			if (excluded(Toolkit.slashToDot(entryName))) {
				continue;
			}
			// logger.debug("Processsing entry : " + entryName);
			try {
				InputStream inputStream = jarFile.getInputStream(jarEntry);
				byte[] classFileBytes = Toolkit.getContents(inputStream).toByteArray();
				ByteArrayOutputStream source = new ByteArrayOutputStream();

				if (jarEntry.getName().indexOf("$") == -1) {
					byte[] sourceFileBytes = null;
					sourceFileBytes = getSource(jarFile, entryName);
					if (sourceFileBytes != null) {
						source.write(sourceFileBytes);
						// logger.debug("Source : " + source.toString());
					}
				}
				processClass(Toolkit.slashToDot(entryName), classFileBytes, source);
			} catch (IOException e) {
				logger.error("Exception reading entry : " + jarEntry + ", from file : " + jarFile, e);
			}
		}
	}

	private byte[] getSource(JarFile jarFile, String entryName) throws IOException {
		// Look for the source
		String javaEntryName = entryName.substring(0, entryName.lastIndexOf('.')) + ".java";
		// logger.debug("Looking for source : " + javaEntryName + ", " + entryName);
		ZipEntry javaEntry = jarFile.getEntry(javaEntryName);
		byte[] sourceFileBytes = null;
		if (javaEntry != null) {
			// logger.debug("Got source : " + javaEntry);
			InputStream inputStream = jarFile.getInputStream(javaEntry);
			sourceFileBytes = Toolkit.getContents(inputStream).toByteArray();
		}
		return sourceFileBytes;
	}

	private void processClass(String name, byte[] classBytes, ByteArrayOutputStream source) {
		if (name != null && name.endsWith(".class")) {
			name = name.substring(0, name.lastIndexOf('.'));
		}
		// logger.debug("Class name : " + name + ", length : " + classBytes.length);
		try {
			VisitorFactory.getClassVisitor(CLASS_ADAPTER_CLASSES, name, classBytes, source);
		} catch (Exception e) {
			logger.error("Exception generating complexity and dependency statistics on class " + name, e);
		}
	}

	private boolean excluded(String name) {
		// Don't process anything that is not a class file or a Java file
		if (!name.endsWith(".class")) {
			logger.debug("Not processing file : " + name);
			return true;
		}
		// Check that the class is included in the included packages
		if (!Configuration.getConfiguration().included(name)) {
			logger.debug("File not included : " + name);
			return true;
		}
		// Don't do excluded classes and packages
		if (Configuration.getConfiguration().excluded(name)) {
			logger.debug("Excluded file : " + name);
			return true;
		}
		// Don't process the same class twice
		if (!filesProcessed.add(name)) {
			logger.debug("Already done file : " + name);
			return true;
		}
		return false;
	}

}