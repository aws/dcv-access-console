package handler.exceptions;

public class BrokerClientException extends RuntimeException {
    private final String message;
    public BrokerClientException(Throwable err) {
        super(err);
        message = "Unexpected broker client error: " + err.getMessage();
    }

    public String getMessage() {
        return message;
    }
}