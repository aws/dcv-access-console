package handler.errors;

public enum GetSessionScreenshotsErrors implements HandlerErrorMessage {
    GET_SESSION_SCREENSHOTS_NO_IDS("GetSessionScreenshots failed: No Ids provided"),
    GET_SESSION_SCREENSHOTS_DEFAULT_MESSAGE("Error while getting session screenshots");


    /**
     * The enum description.
     */
    private final String mDescription;
    GetSessionScreenshotsErrors(final String description) {
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
