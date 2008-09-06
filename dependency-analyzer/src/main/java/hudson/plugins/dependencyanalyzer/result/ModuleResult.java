package hudson.plugins.dependencyanalyzer.result;

import hudson.maven.ModuleName;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ModuleResult implements Serializable {
	private static final long serialVersionUID = -6461651211214230477L;

	String displayName;
	ModuleName moduleName;
	
	Map<DependencyProblemType, List<String>> dependencyProblems;

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public ModuleName getModuleName() {
		return moduleName;
	}

	public void setModuleName(ModuleName moduleName) {
		this.moduleName = moduleName;
	}

	public Map<DependencyProblemType, List<String>> getDependencyProblems() {
		return dependencyProblems;
	}

	public void setDependencyProblems(
			Map<DependencyProblemType, List<String>> dependencyProblems) {
		this.dependencyProblems = dependencyProblems;
	}
}
