package handler.errors;

public enum DescribeUsersErrors implements HandlerErrorMessage {
    DESCRIBE_USERS_DEFAULT_MESSAGE("Error while describing users");



    /**
     * The enum description.
     */
    private final String mDescription;
    DescribeUsersErrors(final String description) {
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
