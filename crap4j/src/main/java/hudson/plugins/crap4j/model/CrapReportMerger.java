package hudson.plugins.crap4j.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.schneide.crap4j.reader.model.IOverallStatistics;

public class CrapReportMerger {
	
	public CrapReportMerger() {
		super();
	}
	
	public ProjectCrapBean mergeReports(ProjectCrapBean previous, ProjectCrapBean... beans) {
		List<IMethodCrap> crapMethods = new ArrayList<IMethodCrap>();
		for (ProjectCrapBean crapBean : beans) {
			Collections.addAll(crapMethods, crapBean.getCrapMethods());
		}
		ProjectCrapBean result = new ProjectCrapBean(
				previous,
				new MergedOverallStatistics(beans),
				crapMethods.toArray(new IMethodCrap[crapMethods.size()]));
		return result;
	}
	
	private static class MergedOverallStatistics implements IOverallStatistics {
		private final ProjectCrapBean[] beans;

		public MergedOverallStatistics(ProjectCrapBean... beans) {
			super();
			this.beans = beans;
		}
		
		public int getCrapLoad() {
			int result = 0;
			for (ProjectCrapBean crapBean : this.beans) {
				result += crapBean.getCrapLoad();
			}
			return result;
		}

		public int getCrapMethodCount() {
			int result = 0;
			for (ProjectCrapBean crapBean : this.beans) {
				result += crapBean.getCrapMethodCount();
			}
			return result;
		}

		public double getCrapMethodPercent() {
			return (getCrapMethodCount() / (double) getMethodCount()) * 100.0d;
		}

		public int getMethodCount() {
			int result = 0;
			for (ProjectCrapBean crapBean : this.beans) {
				result += crapBean.getMethodCount();
			}
			return result;
		}

		public String getName() {
			StringBuilder result = new StringBuilder();
			result.append("report merged from ");
			for (ProjectCrapBean crapBean : this.beans) {
				result.append(crapBean.getName());
				result.append(", ");
			}
			return result.toString();
		}

		public double getTotalCrap() {
			double result = 0;
			for (ProjectCrapBean crapBean : this.beans) {
				result += crapBean.getTotalCrap();
			}
			return result;
		}
	}
}
