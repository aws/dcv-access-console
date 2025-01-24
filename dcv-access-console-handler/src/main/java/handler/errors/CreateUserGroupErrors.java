package handler.errors;

public enum CreateUserGroupErrors implements HandlerErrorMessage {
    AUTHORIZATION_ENGINE_CREATE_FAILED_ERROR("Authorization engine failed to create user group"),
    USER_GROUP_ID_CONFLICT_ERROR("Cannot create user group as the ID already exists"),
    NO_USER_GROUP_ID_ERROR("Request must include a UserGroupId"),
    INVALID_USER_GROUP_ID_ERROR("UserGroupId can only contain alphanumeric characters"),
    CREATE_USER_GROUP_DEFAULT_MESSAGE("Error while creating user group");


    /**
     * The enum description.
     */
    private final String mDescription;
    CreateUserGroupErrors(final String description) {
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
