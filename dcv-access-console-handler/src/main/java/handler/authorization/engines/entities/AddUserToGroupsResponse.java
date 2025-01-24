package handler.authorization.engines.entities;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class AddUserToGroupsResponse {
    List<String> successfulGroups;
    List<String> unsuccessfulGroups;
}
