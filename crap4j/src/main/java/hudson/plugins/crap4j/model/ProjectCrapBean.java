package hudson.plugins.crap4j.model;

import hudson.plugins.crap4j.calculation.CrapDataComparer;
import hudson.plugins.crap4j.display.ICrapComparison;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.schneide.crap4j.reader.model.IMethodCrapData;
import com.schneide.crap4j.reader.model.IOverallStatistics;

public class ProjectCrapBean implements Serializable {
	
	private static final long serialVersionUID = -5174742165631443987L;
	private final IMethodCrap[] crapMethods;
	private final String name;
	private final double totalCrap;
	private final int methodCount;
	private final int crapMethodCount;
	private final double crapMethodPercent;
	private final int crapLoad;
	private final int newCrapMethodsCount;
	private final int fixedCrapMethodsCount;
	
	private transient WeakReference<IMethodCrap[]> newCrapMethods;
	private transient WeakReference<IMethodCrap[]> fixedCrapMethods;
	
	public ProjectCrapBean(
			ProjectCrapBean previousCrap,
			IOverallStatistics statistics,
			IMethodCrapData... allMethodData) {
		this(previousCrap, statistics, extractCrapMethods(statistics, allMethodData));
	}
	
	public ProjectCrapBean(
			ProjectCrapBean previousCrap,
			IOverallStatistics statistics,
			IMethodCrap... crapMethodData) {
		super();
		this.crapMethods = crapMethodData;
		this.name = statistics.getName();
		this.totalCrap = statistics.getTotalCrap();
		this.methodCount = statistics.getMethodCount();
		this.crapMethodCount = statistics.getCrapMethodCount();
		this.crapMethodPercent = statistics.getCrapMethodPercent();
		this.crapLoad = statistics.getCrapLoad();
		// Getting the delta counts to the previous build (if one exists)
		ICrapComparison comparison = compareWithPreviousCrap(previousCrap);
		this.newCrapMethodsCount = comparison.getNewCrapMethods().length;
		this.fixedCrapMethodsCount = comparison.getFixedCrapMethods().length;
		loadCrapMethodComparison(comparison);
	}
	
	private ICrapComparison compareWithPreviousCrap(ProjectCrapBean previousCrap) {
		IMethodCrap[] previousCrapMethods = new IMethodCrap[0];
		if (null != previousCrap) {
			previousCrapMethods = previousCrap.getCrapMethods();
		}
		ICrapComparison comparison = new CrapDataComparer(previousCrapMethods, this.crapMethods);
		return comparison;
	}
	
	private void loadCrapMethodComparison(ProjectCrapBean previousCrap) {
		loadCrapMethodComparison(compareWithPreviousCrap(previousCrap));
	}
	
	public IMethodCrap[] getFixedMethods(ProjectCrapBean previousCrap) {
		if (null == this.fixedCrapMethods) {
			loadCrapMethodComparison(previousCrap);
		}
		if (null == this.fixedCrapMethods.get()) {
			loadCrapMethodComparison(previousCrap);
		}
		return this.fixedCrapMethods.get();
	}
	
	public IMethodCrap[] getNewMethods(ProjectCrapBean previousCrap) {
		if (null == this.newCrapMethods) {
			loadCrapMethodComparison(previousCrap);
		}
		if (null == this.newCrapMethods.get()) {
			loadCrapMethodComparison(previousCrap);
		}
		return this.newCrapMethods.get();
	}
	
	private void loadCrapMethodComparison(ICrapComparison comparison) {
		this.newCrapMethods = new WeakReference<IMethodCrap[]>(comparison.getNewCrapMethods());
		this.fixedCrapMethods = new WeakReference<IMethodCrap[]>(comparison.getFixedCrapMethods());
	}
	
	private static IMethodCrap[] extractCrapMethods(IOverallStatistics statistics, IMethodCrapData... data) {
		checkParameters(statistics, data);
		List<MethodCrapBean> crapBeans = new ArrayList<MethodCrapBean>();
		for (int i = 0; i < data.length; i++) {
			if (data[i].isCrappy()) {
				crapBeans.add(new MethodCrapBean(data[i]));
			}
		}
		return crapBeans.toArray(new MethodCrapBean[crapBeans.size()]);
	}

	private static void checkParameters(IOverallStatistics statistics, IMethodCrapData... data) {
		if (statistics.getMethodCount() == data.length) {
			return;
		}
		StringBuilder message = new StringBuilder();
		message.append("Method count does not match: statistic say ");
		message.append(statistics.getMethodCount());
		message.append(" but there were ");
		message.append(data.length);
		message.append(" data objects given.");
		throw new IllegalArgumentException(message.toString());
	}
	
	public IMethodCrap[] getCrapMethods() {
		return this.crapMethods;
	}

	public String getName() {
		return this.name;
	}

	public double getTotalCrap() {
		return this.totalCrap;
	}

	public int getMethodCount() {
		return this.methodCount;
	}

	public int getCrapMethodCount() {
		return this.crapMethodCount;
	}

	public double getCrapMethodPercent() {
		return this.crapMethodPercent;
	}

	public int getCrapLoad() {
		return this.crapLoad;
	}
	
	public int getNewCrapMethodsCount() {
		return this.newCrapMethodsCount;
	}
	
	public int getFixedCrapMethodsCount() {
		return this.fixedCrapMethodsCount;
	}
}
