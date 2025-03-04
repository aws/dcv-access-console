package authserver.throttling;

public interface AbstractThrottlingService {
    public enum API {
        LOGIN,
        DEFAULT,
    }

    AbstractThrottler getThrottler(String id);

    AbstractThrottler getThrottler(API api, String id);
}
