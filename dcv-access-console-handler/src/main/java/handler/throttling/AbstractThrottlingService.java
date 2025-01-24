package handler.throttling;

public interface AbstractThrottlingService {
    AbstractThrottler getThrottler(String id);
}
