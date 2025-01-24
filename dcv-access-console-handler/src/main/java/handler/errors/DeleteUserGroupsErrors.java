package handler.errors;

public enum DeleteUserGroupsErrors implements HandlerErrorMessage {
    MISSING_REQUIRED_PARAMETERS_ERROR("DeleteUserGroups request must include UserGroupIds and DeleteIfNotEmpty"),
    DELETE_USER_GROUP_DEFAULT_MESSAGE("Error while deleting the user group(s)");


    /**
     * The enum description.
     */
    private final String mDescription;
    DeleteUserGroupsErrors(final String description) {
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
