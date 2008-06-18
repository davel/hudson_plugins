package hudson.plugins.findbugs.util;

/**
 * Indicates an orderly abortion of the processing.
 */
public final class AbortException extends RuntimeException {
    /** Generated ID. */
    private static final long serialVersionUID = -5897876033901702893L;

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message
     *            the detail message (which is saved for later retrieval by the
     *            {@link #getMessage()} method).
     */
    public AbortException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message
     *            the detail message (which is saved for later retrieval by the
     *            {@link #getMessage()} method).
     * @param cause
     *            the cause (which is saved for later retrieval by the
     *            {@link #getCause()} method). (A <tt>null</tt> value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     */
    public AbortException(final String message, final Throwable cause) {
        super(message, cause);
    }
}