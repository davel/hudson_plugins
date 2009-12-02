package hudson.plugins.cppncss;

import hudson.model.AbstractBuild;
import hudson.plugins.cppncss.parser.Statistic;
import hudson.plugins.helpers.health.HealthMetric;

import java.util.Collection;

import org.apache.commons.beanutils.Converter;

/**
 * Created by IntelliJ IDEA. User: stephen Date: 18-Mar-2008 Time: 06:04:17 To change this template use File | Settings
 * | File Templates.
 */
public enum CppNCSSHealthMetrics implements HealthMetric<CppNCSSBuildIndividualReport> {

    TOT_CCN_RATIO {

        public String getName() {
            return "Total CCN";
        }
        public float measure(CppNCSSBuildIndividualReport report) {
            final float ccn = report.getTotals().getFileTotal().getCcn();
            return (ccn);
        }
        
        public float measureNew(CppNCSSBuildIndividualReport observable) {
			return measureNew(this, observable);
		}
        
        public float getBest() {
            return 0;
        }
        public float getWorst() {
            return 1000;
        }
		
    },

    AVG_FILE_CCN_RATIO {

        public String getName() {
            return "Average File CCN";
        }
        public float measure(CppNCSSBuildIndividualReport report) {
            final float ccn = report.getTotals().getFileTotal().getCcn();
            final float number_of_files = report.getResults().getFileResults().size();
            return (ccn/number_of_files);
        }
        
        public float measureNew(CppNCSSBuildIndividualReport observable) {
			return measureNew(this, observable);
		}
        
        public float getBest() {
            return 0;
        }
        public float getWorst() {
            return 1000;
        }
    },
    
    NUMBER_OF_CCN_VIOLATED_FUNCTION {

        public String getName() {
            return "Number of CCN violated functions";
        }
        public float measure(CppNCSSBuildIndividualReport report) {
        	int totalViolations = 0;
        	Collection<Statistic> functionResults = report.getResults().getFunctionResults();
            for (Statistic statistic : functionResults) {
				if(statistic.getCcn() >= report.getFunctionCcnViolationThreshold()){
					totalViolations ++;
				}
			}
            return totalViolations;
        }
        
        public float measureNew(CppNCSSBuildIndividualReport observable) {
			return measureNew(this, observable);
		}
        
        public float getBest() {
            return 0;
        }
        public float getWorst() {
            return 100;
        }
    },
    
    UMBER_OF_NCSS_VIOLATED_FUNCTION {

        public String getName() {
            return "Number of NCSS violated functions";
        }
        public float measure(CppNCSSBuildIndividualReport report) {
        	int totalViolations = 0;
        	Collection<Statistic> functionResults = report.getResults().getFunctionResults();
            for (Statistic statistic : functionResults) {
				if(statistic.getNcss() >= report.getFunctionNcssViolationThreshold()){
					totalViolations ++;
				}
			}
            return totalViolations;
        }
        
        public float measureNew(CppNCSSBuildIndividualReport observable) {
			return measureNew(this, observable);
		}
        
        public float getBest() {
            return 0;
        }
        public float getWorst() {
            return 100;
        }
    };

    static Converter CONVERTER = new Converter() {
        public Object convert(Class aClass, Object o) {
            return valueOf(o.toString());
        }
    };
    
    static float measureNew(CppNCSSHealthMetrics metrics, CppNCSSBuildIndividualReport observable) {
		final float newValue = metrics.measure(observable);
		final float oldValue;
		AbstractBuild<?, ?> previousBuild = (AbstractBuild<?, ?>)observable.getBuild().getPreviousBuild();
		if(previousBuild != null){
			CppNCSSBuildIndividualReport action = (CppNCSSBuildIndividualReport)previousBuild.getAction(AbstractBuildReport.class);
			oldValue = metrics.measure(action);
			return newValue - oldValue;
		}
		return 0;
	};
}
