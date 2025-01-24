package handler.errors;

public enum EditUserGroupErrors implements HandlerErrorMessage {
    MISSING_USER_GROUP_ID("Request must include a UserGroupId"),
    USER_NOT_AUTHORIZED_TO_EDIT_USER_GROUP("User is not authorized to edit user group"),
    EDIT_USER_GROUP_DEFAULT_MESSAGE("Error while editing session template");


    /**
     * The enum description.
     */
    private final String mDescription;
    EditUserGroupErrors(final String description) {
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
