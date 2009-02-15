package com.schneide.crap4j.reader;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.schneide.crap4j.crap.GenericCrapData;
import com.schneide.crap4j.crap.MethodCrapManager;
import com.schneide.crap4j.reader.model.ICrapDetails;
import com.schneide.crap4j.reader.model.ICrapReport;
import com.schneide.crap4j.reader.model.IMethod;
import com.schneide.crap4j.reader.model.IMethodCrapData;
import com.schneide.crap4j.reader.model.IOverallStatistics;
import com.schneide.crap4j.reader.util.NumericalParser;

public class ReportReader {

	private final Reader reportData;
	private final MethodCrapManager methodCrapManager;
	private ParsedCrapReport parsedData;

	public ReportReader(Reader reportData) {
		super();
		this.reportData = reportData;
		this.methodCrapManager = new MethodCrapManager();
		this.parsedData = null;
	}
	
	private class ParsedCrapReport implements ICrapReport {
		private final ParsedCrap4JData data;
		private final ParsedStatistics statistics;

		public ParsedCrapReport(
				ParsedStatistics statistics,
				ParsedCrap4JData data) {
			super();
			this.statistics = statistics;
			this.data = data;
		}
		
		//@Override
		public ICrapDetails getDetails() {
			return this.data;
		}
		
		//@Override
		public IOverallStatistics getStatistics() {
			return this.statistics;
		}
	}
	
	private class ParsedStatistics implements IOverallStatistics {
		private final String name;
		private final int methodCount;
		private final int crapMethodCount;
		private final int crapLoad;
		private final double totalCrap;
		private final double crapMethodPercent;

		public ParsedStatistics(
				String name,
				int methodCount,
				int crapMethodCount,
				int crapLoad,
				double totalCrap,
				double crapMethodPercent) {
			super();
			this.name = name;
			this.methodCount = methodCount;
			this.crapMethodCount = crapMethodCount;
			this.crapLoad = crapLoad;
			this.totalCrap = totalCrap;
			this.crapMethodPercent = crapMethodPercent;
		}

		//@Override
		public int getCrapLoad() {
			return this.crapLoad;
		}

		//@Override
		public int getCrapMethodCount() {
			return this.crapMethodCount;
		}

		//@Override
		public double getCrapMethodPercent() {
			return this.crapMethodPercent;
		}

		//@Override
		public int getMethodCount() {
			return this.methodCount;
		}

		//@Override
		public String getName() {
			return this.name;
		}

		//@Override
		public double getTotalCrap() {
			return this.totalCrap;
		}
	}

	private class ParsedCrap4JData implements ICrapDetails {
		public ParsedCrap4JData() {
			super();
		}

		//@Override
		public MethodCrapManager getMethodCrapManager() {
			return ReportReader.this.methodCrapManager;
		}
	}

	public synchronized ICrapReport parseData() throws IOException {
		if (null != this.parsedData) {
			return this.parsedData;
		}
		try {
			SAXBuilder saxBuilder = new SAXBuilder(false);
			Document build = saxBuilder.build(this.reportData);
			Element rootElement = build.getRootElement();
			ParsedStatistics statistics = parseStatistics(rootElement);
			ParsedCrap4JData details = parseDetails(rootElement);
			this.parsedData = new ParsedCrapReport(statistics, details);
			return this.parsedData;
		} catch (JDOMException e) {
			throw new IOException(e.getMessage());
		}
	}
	
	private ParsedStatistics parseStatistics(Element rootElement) {
		Element statsElement = rootElement.getChild("stats"); //$NON-NLS-1$
		NumericalParser parser = new NumericalParser();
		ParsedStatistics result = new ParsedStatistics(
				readTextualContent(statsElement, "name"),
				parser.parseInt(readTextualContent(statsElement, "methodCount")),
				parser.parseInt(readTextualContent(statsElement, "crapMethodCount")),
				parser.parseInt(readTextualContent(statsElement, "crapLoad")),
				parser.parseDouble(readTextualContent(statsElement, "totalCrap")),
				parser.parseDouble(readTextualContent(statsElement, "crapMethodPercent")));
		return result;
	}

	@SuppressWarnings("unchecked")
	private ParsedCrap4JData parseDetails(Element rootElement) {
		Element methodsElement = rootElement.getChild("methods"); //$NON-NLS-1$
		List methodElements = methodsElement.getChildren("method"); //$NON-NLS-1$
		for (Object object : methodElements) {
			IMethodCrapData crapData = parseMethodCrap((Element) object);
			this.methodCrapManager.addMethodCrapData(crapData);
		}
		return new ParsedCrap4JData();
	}

	private IMethodCrapData parseMethodCrap(Element methodElement) {
		IMethod method = parseMethod(methodElement);
		NumericalParser parser = new NumericalParser();
		double crap = parser.parseDouble(readTextualContent(methodElement, "crap")); //$NON-NLS-1$
		int crapLoad = parser.parseInt(readTextualContent(methodElement, "crapLoad")); //$NON-NLS-1$
		double coverage = parser.parseDouble(readTextualContent(methodElement, "coverage")); //$NON-NLS-1$
		double complexity = parser.parseDouble(readTextualContent(methodElement, "complexity")); //$NON-NLS-1$
		return new MethodCrapData(method,
				crap, crapLoad, coverage, complexity);
	}

	private String readTextualContent(Element element, String subTagName) {
		String childText = element.getChildText(subTagName);
		if (null == childText) {
			System.err.println("Got null content for " + subTagName);
			return null;
		}
		return childText.trim();
	}

	private class MethodCrapData extends GenericCrapData<IMethod> implements IMethodCrapData {
		public MethodCrapData(IMethod context, double crap, int crapLoad,
				double coverage, double complexity) {
			super(context, 0, 1, crap, crapLoad, coverage, complexity);
		}

		@Override
		public int getCrappyMethods() {
			if (isCrappy()) {
				return 1;
			}
			return 0;
		}

		//@Override
		public boolean isCrappy() {
			return (getCrapLoad() > 0);
		}

		@Override
		public double getCrapPercentage() {
			if (isCrappy()) {
				return 1.0d;
			}
			return 0.0d;
		}
	}

	private IMethod parseMethod(Element methodElement) {
		return new ParsedMethod(
				readTextualContent(methodElement, "package"),
				readTextualContent(methodElement, "className"),
				readTextualContent(methodElement, "methodName"), //$NON-NLS-1$
				readTextualContent(methodElement, "methodSignature"), //$NON-NLS-1$
				readTextualContent(methodElement, "fullMethod")); //$NON-NLS-1$
	}
	
	private static class ParsedMethod implements IMethod {
		private final String packageName;
		private final String className;
		private final String methodName;
		private final String signature;
		private final String fullMethod;

		public ParsedMethod(
				String packageName,
				String className,
				String methodName,
				String signature,
				String fullMethod) {
			super();
			this.packageName = packageName;
			this.className = className;
			this.methodName = methodName;
			this.signature = signature;
			this.fullMethod = fullMethod;
		}

		//@Override
		public String getClassName() {
			return this.className;
		}

		//@Override
		public String getFullMethod() {
			return this.fullMethod;
		}

		//@Override
		public String getName() {
			return this.methodName;
		}

		//@Override
		public String getPackageName() {
			return this.packageName;
		}

		//@Override
		public String getSignature() {
			return this.signature;
		}
	}
}
