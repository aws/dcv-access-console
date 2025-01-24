package handler.errors;

public enum DescribeSessionTemplatesErrors implements HandlerErrorMessage {
    DESCRIBE_SESSION_TEMPLATES_DEFAULT_MESSAGE("Error while describing session templates");



    /**
     * The enum description.
     */
    private final String mDescription;
    DescribeSessionTemplatesErrors(final String description) {
        this.mDescription = description;
    }

    /**
     * Gets the description.
     * @return the description
     */
    public String getDescription() {
        return this.mDescription;
    }
}
