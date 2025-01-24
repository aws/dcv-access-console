package handler.errors;

public enum EditSessionTemplateErrors implements HandlerErrorMessage {
    MISSING_TEMPLATE_ID("TemplateId is required"),
    MISSING_REQUEST_DATA("CreateSessionTemplateRequestData is required"),
    USER_NOT_AUTHORIZED_TO_EDIT_TEMPLATE("User is not authorized to edit this session template"),
    EDIT_SESSION_TEMPLATE_DEFAULT_MESSAGE("Error while editing session template");


    /**
     * The enum description.
     */
    private final String mDescription;
    EditSessionTemplateErrors(final String description) {
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
