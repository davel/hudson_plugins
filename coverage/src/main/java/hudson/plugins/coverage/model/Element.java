package hudson.plugins.coverage.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents an element in the source code.
 *
 * @author Stephen Connolly
 * @since 26-Jun-2008 17:04:11
 */
public final class Element implements Comparable<Element> {
// ------------------------------ FIELDS ------------------------------

    /**
     * The parent source code element.
     */
    private final Element parent;

    /**
     * The name of this element.
     */
    private final String name;

    /**
     * {@code true} if the element has a one-to-one correspondance with source code files.
     */
    private final boolean fileLevel;

    /**
     * The child elements.
     */
    private final transient Set<Element> children = new TreeSet<Element>();

    private final transient Model model;

    /**
     * Lazily calculated full name of the element.
     */
    private transient String fullName = null;

    /**
     * Lazily calculated flag to indicate that this element is below the file level.
     */
    private transient Boolean subfileLevel = null;

// -------------------------- STATIC METHODS --------------------------

    /**
     * Gets the root element from which all elements must inherit.
     *
     * @return the root element from which all elements must inherit.
     */
    public static Element getRootElement() {
        return SingletonHolder.ROOT;
    }

    /**
     * Gets the root element from which all elements must inherit.
     *
     * @param fullName The full name of the element.
     *
     * @return the root element from which all elements must inherit.
     */
    public static Element getElement(String fullName) {
        return SingletonHolder.ROOT.find(fullName);
    }

    /**
     * Finds an element from its full name.
     *
     * @param fullName The full name.
     *
     * @return the element of {@code null} if the element does not exist.
     */
    public Element find(String fullName) {
        if (getFullName().equals(fullName)) {
            return this;
        }
        for (Element element : children) {
            if (fullName.startsWith(element.getFullName())) {
                return element.find(fullName);
            }
        }
        return null;
    }

    /**
     * Creates a new source code element.
     *
     * @param parent    The parent.
     * @param name      The name of the element.
     * @param fileLevel {@code true} if this element corresponds to a source file.
     * @param model     The model that describes how this element calculates results from its children.
     *
     * @return The new element.
     */
    public static Element newElement(Element parent, String name, boolean fileLevel, Model model) {
        Element result = new Element(parent, name, fileLevel, model);
        parent.addChild(result);
        return result;
    }

    /**
     * Adds a child to this, it's parent.
     *
     * @param child the child.
     */
    private synchronized void addChild(Element child) {
        if (child.getParent() != this) {
            throw new IllegalArgumentException("Cannot add the child of a different parent");
        }
        children.add(child);
    }

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Constructor for root element.
     */
    private Element() {
        name = "";
        parent = null;
        fileLevel = false;
        model = StandardModel.getInstance();
    }

    /**
     * Constructor for a child element.
     *
     * @param parent    The parent.
     * @param name      The name.
     * @param fileLevel {@code true} if this is a file level element.
     * @param model     The model that describes how this element calculates results from its children.
     */
    private Element(Element parent, String name, boolean fileLevel, Model model) {
        parent.getClass(); // throw NPE if null
        name.getClass(); // throw NPE if null
        model.getClass(); // throw NPE if null
        if (name.indexOf('/') != -1) {
            throw new IllegalArgumentException("The name of an element caonnot contain the '/' character");
        }
        this.name = name;
        this.parent = parent;
        this.fileLevel = fileLevel;
        this.model = model;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * Gets the full name for the element.
     *
     * @return the full name for the element.
     */
    public String getFullName() {
        // don't need to worry about multiple threads as the result must be the same.
        if (fullName == null) {
            LinkedList<String> names = new LinkedList<String>();
            for (Element i = this; i.getParent() != null; i = i.getParent()) {
                names.addFirst(i.getName());
            }
            StringBuilder buf = new StringBuilder();
            boolean first = true;
            for (String name : names) {
                buf.append('/');
                buf.append(name);
            }
            fullName = buf.toString();
        }
        return fullName;
    }

    /**
     * Getter for property 'model'.
     *
     * @return Value for property 'model'.
     */
    public Model getModel() {
        return model;
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
     * Getter for property 'parent'.
     *
     * @return Value for property 'parent'.
     */
    public Element getParent() {
        return parent;
    }

    /**
     * Getter for property 'fileLevel'.
     *
     * @return Value for property 'fileLevel'.
     */
    public boolean isFileLevel() {
        return fileLevel;
    }

// ------------------------ CANONICAL METHODS ------------------------

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Element element = (Element) o;

        if (!name.equals(element.name)) {
            return false;
        }
        if (parent != null ? !parent.equals(element.parent) : element.parent != null) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int result;
        result = (parent != null ? parent.hashCode() : 0);
        result = 31 * result + name.hashCode();
        return result;
    }

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface Comparable ---------------------

    /**
     * {@inheritDoc}
     */
    public int compareTo(Element that) {
        if (parent == that.parent || (parent != null && parent.equals(that.parent))) {
            // same parent, so compare based on name
            return name.compareTo(that.name);
        }
        if (parent != null && that.parent != null) {
            // has two parents and both are not the root, so compare parents
            return parent.compareTo(that.parent);
        }
        if (parent == null) {
            // this is nearest the root, so comes first
            assert that.parent != null;
            return -1;
        }
        // that is nearest the root, so comes first.
        return +1;
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * Destroys this element. Needed for unit tests.
     */
    synchronized void destroy() {
        // first destroy all children
        for (Element child : new TreeSet<Element>(children)) {
            child.destroy();
        }
        // next unhook ourselves from the parent
        if (parent != null) {
            parent.removeChild(this);
        }
    }

    /**
     * Removes a child element.
     *
     * @param element The child.
     */
    private synchronized void removeChild(Element element) {
        children.remove(element);
    }

    /**
     * Getter for property 'children'.
     *
     * @return Value for property 'children'.
     */
    public synchronized Set<Element> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    /**
     * Getter for property 'subfileLevel'.
     *
     * @return Value for property 'subfileLevel'.
     */
    public boolean isSubfileLevel() {
        // don't need to worry about multiple threads as the result must be the same.
        if (subfileLevel == null) {
            if (fileLevel || this.getParent() == null) {
                subfileLevel = Boolean.FALSE;
            } else {
                subfileLevel = Boolean.valueOf(getParent().isSubfileLevel());
            }
        }
        return subfileLevel.booleanValue();
    }

    /**
     * Creates a new source code element as a child of {@code this}.
     *
     * @param name      The name of the element.
     * @param fileLevel {@code true} if this element corresponds to a source file.
     * @param model     The model that describes how this element calculates results from its children.
     *
     * @return The new element.
     */
    public Element newChild(String name, boolean fileLevel, Model model) {
        Element result = new Element(this, name, fileLevel, model);
        addChild(result);
        return result;
    }

    @Override
    public String toString() {
        return getFullName();
    }

    // -------------------------- INNER CLASSES --------------------------

    /**
     * Holds the root element and ensures that the Element class has fully loaded before constructing it.
     */
    private static final class SingletonHolder {
// ------------------------------ FIELDS ------------------------------

        /**
         * The root element singleton.
         */
        private static final Element ROOT = new Element();

// --------------------------- CONSTRUCTORS ---------------------------

        /**
         * Do not instantiate SingletonHolder.
         */
        private SingletonHolder() {
        }
    }
}
