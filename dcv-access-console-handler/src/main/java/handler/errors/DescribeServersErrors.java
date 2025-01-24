package handler.errors;

public enum DescribeServersErrors implements HandlerErrorMessage {
    DESCRIBE_SERVERS_DEFAULT_MESSAGE("Error while describing servers");


    /**
     * The enum description.
     */
    private final String mDescription;
    DescribeServersErrors(final String description) {
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
