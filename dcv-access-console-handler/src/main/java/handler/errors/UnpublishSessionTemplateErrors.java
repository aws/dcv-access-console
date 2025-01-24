package handler.errors;

public enum UnpublishSessionTemplateErrors implements HandlerErrorMessage {
    UNPUBLISH_SESSION_TEMPLATE_NO_IDS("Session Template IDs are required"),
    USER_UNAUTHORIZED_TO_UNPUBLISH_SESSION_TEMPLATE("User is not authorized to unpublish this session template"),
    UNPUBLISH_SESSION_TEMPLATE_DEFAULT_MESSAGE("Error while unpublishing session template");


    /**
     * The enum description.
     */
    private final String mDescription;
    UnpublishSessionTemplateErrors(final String description) {
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
