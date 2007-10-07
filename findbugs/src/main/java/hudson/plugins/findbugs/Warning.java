package hudson.plugins.findbugs;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * A FindBugs warning.
 */
public class Warning implements Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -3694883222707674470L;
    /** Type of warning. */
    private String type;
    /** Category of warning. */
    private String category;
    /** Priority of warning. */
    private String priority;
    /** Message of warning. */
    private String message;
    /** Line number of warning. */
    private String lineNumber;
    /** Corresponding Java class. */
    private JavaClass javaClass;
    /** Corresponding qualified class name. */
    private String qualifiedName;
    /** Filename of the java source. */
    private String fileName;

    /**
     * Returns the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Links this warning to the specified class. This class is only considered if it
     * is not a role class and if it is the first class.
     *
     * @param owningClass the class that contains this warning
     */
    public void linkClass(final JavaClass owningClass) {
        if (!owningClass.isRoleClass() && javaClass == null) {
            javaClass = owningClass;
            setQualifiedName(owningClass.getClassname());
            setLineNumber(javaClass.getLineNumber());
            setFile(javaClass.getFileName());
        }
    }

    /**
     * Returns the javaClass.
     *
     * @return the javaClass
     */
    public JavaClass getJavaClass() {
        return javaClass;
    }

    /**
     * Returns the bug pattern description.
     *
     * @return the bug pattern description.
     */
    public String getDescription() {
        return FindBugsMessages.getInstance().getMessage(getType());
    }

    /**
     * Sets the type to the specified value.
     *
     * @param type the value to set
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Returns the category.
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category to the specified value.
     *
     * @param category the value to set
     */
    public void setCategory(final String category) {
        this.category = category;
    }

    /**
     * Sets the priority to the specified value.
     *
     * @param priority the value to set
     */
    public void setPriority(final String priority) {
        this.priority = priority;
    }

    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message to the specified value.
     *
     * @param message the value to set
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * Returns the lineNumer.
     *
     * @return the lineNumer
     */
    public String getLineNumber() {
        return StringUtils.defaultIfEmpty(lineNumber, "Not available");
    }

    /**
     * Sets the lineNumer to the specified value.
     *
     * @param lineNumber the value to set
     */
    public void setLineNumber(final String lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * Gets the priority.
     *
     * @return the priority
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Returns the qualifiedName.
     *
     * @return the qualifiedName
     */
    public String getQualifiedName() {
        return qualifiedName;
    }

    /**
     * Returns the qualifiedName.
     *
     * @return the qualifiedName
     */
    public String getPackageName() {
        return StringUtils.substringBeforeLast(qualifiedName, ".");
    }

    /**
     * Returns the classname.
     *
     * @return the classname
     */
    public String getClassname() {
        return StringUtils.substringAfterLast(qualifiedName, ".");
    }

    // CHECKSTYLE:OFF
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((qualifiedName == null) ? 0 : qualifiedName.hashCode());
        result = prime * result + ((lineNumber == null) ? 0 : lineNumber.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Warning other = (Warning)obj;
        if (qualifiedName == null) {
            if (other.qualifiedName != null) {
                return false;
            }
        }
        else if (!qualifiedName.equals(other.qualifiedName)) {
            return false;
        }
        if (lineNumber == null) {
            if (other.lineNumber != null) {
                return false;
            }
        }
        else if (!lineNumber.equals(other.lineNumber)) {
            return false;
        }
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        }
        else if (!message.equals(other.message)) {
            return false;
        }
        return true;
    }

    /**
     * Sets the fully qualified name of the containing class.
     *
     * @param name
     *            the name of the class
     */
    public void setQualifiedName(final String name) {
        qualifiedName = name;
    }

    /**
     * Sets a reference to the file where this warning is found.
     *
     * @param file the file name
     */
    public void setFile(final String file) {
        fileName = file.replace('/', '!').replace('\\', '!');
    }

    /**
     * Gets the filename of this warning.
     *
     * @return the file
     */
    public String getFile() {
        return fileName;
    }

    /**
     * Returns whether a valid filename is available for this warning.
     *
     * @return <code>true</code> if a valid filename is available for this
     *         warning
     */
    public boolean hasFile() {
        return fileName != null;
    }
}

