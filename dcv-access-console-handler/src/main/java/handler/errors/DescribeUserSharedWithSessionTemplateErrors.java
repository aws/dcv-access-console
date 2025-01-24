package handler.errors;

public enum DescribeUserSharedWithSessionTemplateErrors implements HandlerErrorMessage {
    USER_UNAUTHORIZED_ERROR("User is not authorized to describe users published to this session template"),
    SESSION_TEMPLATE_ID_NULL("Session Template ID cannot be null"),
    DESCRIBE_USER_SHARED_WITH_SESSION_TEMPLATE_DEFAULT_MESSAGE("Error while describing users shared with session template");


    /**
     * The enum description.
     */
    private final String mDescription;
    DescribeUserSharedWithSessionTemplateErrors(final String description) {
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
