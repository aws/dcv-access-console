package handler.authorization.engines.entities;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SetShareListResponse {
    List<String> successfulUsers;
    List<String> unSuccessfulUsers;
    List<String> successfulGroups;
    List<String> unSuccessfulGroups;
}
