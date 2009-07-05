package hudson.plugins.crap4j.model;

import hudson.plugins.crap4j.calculation.CrapDataComparer;
import hudson.plugins.crap4j.display.ICrapComparison;

import java.io.Serializable;
import java.lang.ref.WeakReference;

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
