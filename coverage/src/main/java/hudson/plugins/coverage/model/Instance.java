package hudson.plugins.coverage.model;

import java.io.File;
import java.util.*;

/**
 * An instance of an {@linkplain Element}.
 *
 * @author Stephen Connolly
 * @since 26-Jun-2008 21:45:31
 */
public class Instance {
// ------------------------------ FIELDS ------------------------------

    /**
     * The parent instance.
     */
    private final Instance parent;

    /**
     * The instance name.
     */
    private final String name;

    /**
     * The instance element type.
     */
    private final Element element;

    /**
     * The instance's children.
     */
    private final Map<Element, Map<String, Instance>> children =
            new TreeMap<Element, Map<String, Instance>>();

    /**
     * The instance's measurements.
     */
    private final Map<Metric, Measurement> measurements = new HashMap<Metric, Measurement>();

    /**
     * Recorders registered against this instance during parsing.
     */
    private final transient Map<Recorder, Object> recorders = new HashMap<Recorder, Object>();

    /**
     * Used to keep track of multiple recorders mapping the same file to different models. Only set for file level
     * elements while on the slave that hosts the source code.
     */
    private transient File sourceFile;

    /**
     * Used to keep track of all the source code files so we can archive the source code.
     * Only set for the root element while on the slave that hosts the source code.
     */
    private final transient Set<File> sourceFiles;

    /**
     * Used to keep track of all the mesurement files and their associated recorders.
     */
    private final transient Map<Recorder, Set<File>> measurementFiles;

// -------------------------- STATIC METHODS --------------------------

    /**
     * Creates a new Instance instance using the provided recorders.
     *
     * @param recorders The recorders.
     * @return The new Instance instance.
     */
    public static Instance newInstance(Set<Recorder> recorders) {
        Instance result = new Instance();

        // first-pass: identify the source files
        for (Recorder recorder : recorders) {
            recorder.identifySourceFiles(result);
        }

        // second-pass: parse the coverage results
        result.parseSourceResults();

        // third-pass: consolidate the results
        result.applyModels();

        // done
        return result;
    }

    /**
     * Apply the model to the results
     */
    private void applyModels() {
        for (Element child : element.getChildren()) {
            for (Instance childInstance : getChildren(child).values()) {
                childInstance.applyModels();
            }
        }
        element.getModel().apply(this);
    }

    /**
     * Second-pass parsing.
     */
    private synchronized void parseSourceResults() {
        if (element.isSubfileLevel()) {
            throw new IllegalStateException("Should never get here");
        }
        if (element.isFileLevel()) {
            for (Map.Entry<Recorder, Object> recorder : recorders.entrySet()) {
                recorder.getKey().parseSourceResults(this, measurementFiles.get(recorder), recorder.getValue());
            }
            recorders.clear();
        } else {
            for (Element child : element.getChildren()) {
                for (Instance childInstance : getChildren(child).values()) {
                    childInstance.parseSourceResults();
                }
            }
        }
    }

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Constructs a new root Instance instance.
     */
    private Instance() {
        this.element = Element.getRootElement();
        this.name = "";
        this.parent = null;
        for (Element child : this.element.getChildren()) {
            children.put(child, Collections.synchronizedMap(new TreeMap<String, Instance>()));
        }
        this.sourceFile = null;
        this.sourceFiles = new HashSet<File>();
        this.measurementFiles = new HashMap<Recorder, Set<File>>();
    }

    /**
     * Constructs a new Instance instance.
     *
     * @param element The element type of the new instance.
     * @param parent  The parent instance.
     * @param name    The name of the instance.
     */
    private Instance(Element element, Instance parent, String name) {
        element.getClass(); // throw NPE if null
        parent.getClass(); // throw NPE if null
        name.getClass(); // throw NPE if null
        if (element.isFileLevel()) {
            throw new IllegalArgumentException("You must specify the source file for a file level element");
        }
        this.element = element;
        this.name = name;
        this.parent = parent;
        this.sourceFile = null;
        for (Element child : element.getChildren()) {
            children.put(child, Collections.synchronizedMap(new TreeMap<String, Instance>()));
        }
        this.sourceFiles = null;
        this.measurementFiles = null;
    }

    /**
     * Constructs a new Instance instance at the file level.
     *
     * @param element    The element type of the new instance.
     * @param parent     The parent instance.
     * @param name       The name of the instance.
     * @param sourceFile The source code that this file element corresponds to.
     */
    private Instance(Element element, Instance parent, String name, File sourceFile) {
        element.getClass(); // throw NPE if null
        parent.getClass(); // throw NPE if null
        name.getClass(); // throw NPE if null
        sourceFile.getClass(); // throw NPE if null
        if (!element.isFileLevel()) {
            throw new IllegalArgumentException("You can only specify the source file for a file level element");
        }
        this.element = element;
        this.name = name;
        this.parent = parent;
        this.sourceFile = sourceFile;
        for (Element child : element.getChildren()) {
            children.put(child, Collections.synchronizedMap(new TreeMap<String, Instance>()));
        }
        this.sourceFiles = null;
        this.measurementFiles = new HashMap<Recorder, Set<File>>();
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * Getter for property 'parent'.
     *
     * @return Value for property 'parent'.
     */
    public Instance getParent() {
        return parent;
    }

    /**
     * Getter for property 'name'.
     *
     * @return Value for property 'name'.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the element type of this instance.
     *
     * @return the element type of this instance.
     */
    public Element getElement() {
        return element;
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * Registers a recorder.
     *
     * @param recorder         The recorder.
     * @param measurementFiles The measurement files.
     * @param memo             A memo object that the recorder can use to hold state prior to the second-pass parsing
     *                         {@linkplain hudson.plugins.coverage.model.Recorder#parseSourceResults(Instance, Set,
     *                         Object)}
     * @see hudson.plugins.coverage.model.Recorder#identifySourceFiles(Instance) the first-pass parsing which should
     *      register recorders.
     * @see hudson.plugins.coverage.model.Recorder#reidentifySourceFiles(Instance, java.util.Set, java.io.File) the
     *      first-pass parsing at report time which should register recorders.
     * @see hudson.plugins.coverage.model.Recorder#parseSourceResults(Instance, Set, Object) the second-pass parsing which
     *      populates the parse results.
     */
    public synchronized void addRecorder(Recorder recorder, Set<File> measurementFiles, Object memo) {
        recorder.getClass(); // throw NPE if null
        measurementFiles.getClass(); // throw NPE if null
        if (!element.isFileLevel()) {
            throw new IllegalStateException("Cannot add a recorder except at the file level");
        }
        if (recorders.containsKey(recorder)) {
            throw new IllegalStateException("Cannot add a recorder except at the file level");
        }
        recorders.put(recorder, memo);
        Instance root = this.parent;
        assert root != null : "The root element can never be source level";
        assert this.measurementFiles != null;
        synchronized (this.measurementFiles) {
            this.measurementFiles.put(recorder, measurementFiles);
        }
        while (root.measurementFiles != null) {
            root = root.parent;
        }
        synchronized (root.measurementFiles) {
            if (root.measurementFiles.containsKey(recorder)) {
                root.measurementFiles.get(recorder).addAll(measurementFiles);
            } else {
                root.measurementFiles.put(recorder, new HashSet<File>(measurementFiles));
            }
        }
    }

    /**
     * Returns all the child element types.
     *
     * @return all the child element types.
     */
    public Set<Element> getChildElements() {
        return Collections.unmodifiableSet(children.keySet());
    }

    /**
     * Returns all the children of a specific child element type.
     *
     * @param element The child element type.
     * @return All the children of the child element type.
     */
    public Map<String, Instance> getChildren(Element element) {
        if (!this.element.getChildren().contains(element)) {
            throw new IllegalArgumentException("A " + element + " is not a child of " + this.element);
        }
        return Collections.unmodifiableMap(children.get(element));
    }

    /**
     * Returns the measurement of a specific metric.
     *
     * @param metric The metric.
     * @return The measurement of the metric.
     */
    public Measurement getMeasurement(Metric metric) {
        metric.getClass();
        return measurements.get(metric);
    }

    /**
     * Returns all the measurements.
     *
     * @return all the measurements.
     */
    public Map<Metric, Measurement> getMeasurements() {
        return Collections.unmodifiableMap(measurements);
    }

    /**
     * Returns the available metrics on this instance.
     *
     * @return the available metrics on this instance.
     */
    public Set<Metric> getMetrics() {
        return Collections.unmodifiableSet(measurements.keySet());
    }

    /**
     * Returns the recorders that are registered with this instance.
     *
     * @return The recorders.
     */
    public synchronized Set<Recorder> getRecorders() {
        if (!element.isFileLevel()) {
            throw new IllegalStateException("Recorders are only supported at the file level");
        }
        return Collections.unmodifiableSet(recorders.keySet());
    }

    /**
     * Creates a new child instance.
     *
     * @param element The child element type.
     * @param name    The child name.
     * @return The child instance.
     */
    public Instance newChild(Element element, String name) {
        Instance child = new Instance(element, this, name);
        addChild(child);
        return child;
    }

    /**
     * Creates a new child instance corresponding with a file level element.
     *
     * @param element    The child element type.
     * @param name       The child name.
     * @param sourceFile The source file.
     * @return The child instance.
     */
    public Instance newChild(Element element, String name, File sourceFile) {
        Instance child = new Instance(element, this, name, sourceFile);
        addChild(child);
        return child;
    }

    /**
     * Add's a child instance.
     *
     * @param child The child.
     */
    private void addChild(Instance child) {
        child.getClass(); // throw NPE if null
        if (!element.getChildren().contains(child.element)) {
            throw new IllegalArgumentException("A " + child.element + " is not a child of " + element);
        }
        final Map<String, Instance> map = children.get(child.element);
        map.put(child.name, child);
        if (child.sourceFile != null) {
            Instance root = this;
            while (root.sourceFiles != null) {
                root = root.parent;
            }
            synchronized (root.sourceFiles) {
                root.sourceFiles.add(sourceFile);
            }
        }
    }

    /**
     * Set's the measurement of a specific metric
     *
     * @param metric      The metric.
     * @param measurement The metric's measurement.
     */
    public void setMeasurement(Metric metric, Measurement measurement) {
        metric.getClass();
        measurement.getClass();
        if (!metric.getClazz().isInstance(measurement)) {
            throw new IllegalArgumentException("Measurements of " + metric + " must implement " + metric.getClazz());
        }
        measurements.put(metric, measurement);
    }
}
