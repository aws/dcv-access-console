package handler.errors;

public enum GetSessionConnectionDataErrors implements HandlerErrorMessage {
    UNAUTHORIZED_TO_CONNECT_AS_OTHER("User is not authorized to get session connection data for other users"),
    UNAUTHORIZED_TO_CONNECT_TO_SESSION("User is not authorized to get session connection for this session"),
    GET_SESSION_CONNECTION_DATA_DEFAULT_MESSAGE("Error while getting session connection data");


    /**
     * The enum description.
     */
    private final String mDescription;
    GetSessionConnectionDataErrors(final String description) {
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
