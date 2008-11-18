package hudson.plugins.coverage.model;

import hudson.plugins.coverage.model.measurements.BasicCoverage;
import hudson.plugins.coverage.model.measurements.LineCoverage;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 26-Jun-2008 18:01:36
 */
public class JavaModel implements Model {

    public static final Element LANGUAGE = Element.getRootElement().newChild("java", false, SingletonHolder.INSTANCE);
    public static final Element PACKAGE = LANGUAGE.newChild("package", false, SingletonHolder.INSTANCE);
    public static final Element FILE = PACKAGE.newChild("file", true, SingletonHolder.INSTANCE);
    public static final Element CLASS = FILE.newChild("class", false, SingletonHolder.INSTANCE);
    public static final Element METHOD = CLASS.newChild("method", false, SingletonHolder.INSTANCE);

    public static final Metric PACKAGE_COVERAGE = Metric.newMetric("package", BasicCoverage.class);
    public static final Metric FILE_COVERAGE = Metric.newMetric("file", BasicCoverage.class);
    public static final Metric CLASS_COVERAGE = Metric.newMetric("class", BasicCoverage.class);
    public static final Metric METHOD_COVERAGE = Metric.newMetric("method", BasicCoverage.class);
    public static final Metric LINE_COVERAGE = Metric.LINE_COVERAGE;
    public static final Metric BRANCH_COVERAGE = Metric.BRANCH_COVERAGE;

    public void apply(Instance instance) {
        StandardModel.getInstance().apply(instance);
        if (LANGUAGE.equals(instance.getElement())) {
            int methodCount = 0;
            int methodCover = 0;
            int classCount = 0;
            int classCover = 0;
            int fileCount = 0;
            int fileCover = 0;
            for (Instance child : instance.getChildren(PACKAGE).values()) {
                BasicCoverage coverage = (BasicCoverage) child.getMeasurement(METHOD_COVERAGE);
                methodCount += coverage.getCount();
                methodCover += coverage.getCover();
                coverage = (BasicCoverage) child.getMeasurement(CLASS_COVERAGE);
                classCount += coverage.getCount();
                classCover += coverage.getCover();
                if (coverage.getCover() > 0 || coverage.getCount() == 0) {
                    fileCover++;
                }
                fileCount++;
            }
            instance.setMeasurement(METHOD_COVERAGE, new BasicCoverage(methodCount, methodCover));
            instance.setMeasurement(CLASS_COVERAGE, new BasicCoverage(classCount, classCover));
            instance.setMeasurement(FILE_COVERAGE, new BasicCoverage(fileCount, fileCover));
        } else if (PACKAGE.equals(instance.getElement())) {
            int methodCount = 0;
            int methodCover = 0;
            int classCount = 0;
            int classCover = 0;
            int fileCount = 0;
            int fileCover = 0;
            for (Instance child : instance.getChildren(FILE).values()) {
                BasicCoverage coverage = (BasicCoverage) child.getMeasurement(METHOD_COVERAGE);
                methodCount += coverage.getCount();
                methodCover += coverage.getCover();
                coverage = (BasicCoverage) child.getMeasurement(CLASS_COVERAGE);
                classCount += coverage.getCount();
                classCover += coverage.getCover();
                if (coverage.getCover() > 0 || coverage.getCount() == 0) {
                    fileCover++;
                }
                fileCount++;
            }
            instance.setMeasurement(METHOD_COVERAGE, new BasicCoverage(methodCount, methodCover));
            instance.setMeasurement(CLASS_COVERAGE, new BasicCoverage(classCount, classCover));
            instance.setMeasurement(FILE_COVERAGE, new BasicCoverage(fileCount, fileCover));
        } else if (FILE.equals(instance.getElement())) {
            int methodCount = 0;
            int methodCover = 0;
            int classCount = 0;
            int classCover = 0;
            for (Instance child : instance.getChildren(CLASS).values()) {
                final BasicCoverage coverage = (BasicCoverage) child.getMeasurement(METHOD_COVERAGE);
                methodCount += coverage.getCount();
                methodCover += coverage.getCover();
                if (coverage.getCover() > 0 || coverage.getCount() == 0) {
                    classCover++;
                }
                classCount++;
            }
            instance.setMeasurement(METHOD_COVERAGE, new BasicCoverage(methodCount, methodCover));
            instance.setMeasurement(CLASS_COVERAGE, new BasicCoverage(classCount, classCover));
        } else if (CLASS.equals(instance.getElement())) {
            int count = 0;
            int cover = 0;
            for (Instance child : instance.getChildren(METHOD).values()) {
                final LineCoverage coverage = (LineCoverage) child.getMeasurement(LINE_COVERAGE);
                if (coverage.getCover() > 0 || coverage.getCount() == 0) {
                    cover++;
                }
                count++;
            }
            instance.setMeasurement(METHOD_COVERAGE, new BasicCoverage(count, cover));
        } else if (METHOD.equals(instance.getElement())) {
            // ignore as methods only have line & branch
        }
    }

    private static final class SingletonHolder {

        private static final JavaModel INSTANCE = new JavaModel();
    }

}
