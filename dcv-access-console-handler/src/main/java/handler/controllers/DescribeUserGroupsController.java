package handler.controllers;

import handler.api.DescribeUserGroupsApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.model.DescribeUserGroupsRequestData;
import handler.model.DescribeUserGroupsResponse;
import handler.model.Error;
import handler.model.UserGroup;
import handler.persistence.UserGroupEntity;
import handler.services.UserGroupService;
import handler.utils.NextToken;
import handler.utils.Sort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.DescribeUserGroupsErrors.DESCRIBE_USER_GROUPS_DEFAULT_MESSAGE;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DescribeUserGroupsController implements DescribeUserGroupsApi {

    private final UserGroupService userGroupService;
    private final Sort<DescribeUserGroupsRequestData, UserGroup> userGroupSort;
    private final AbstractAuthorizationEngine authorizationEngine;

    @Value("${persistence-db-default-max-results:20}")
    private int defaultMaxResults;

    private ResponseEntity<DescribeUserGroupsResponse> sendExceptionResponse(HttpStatus status, Exception e, DescribeUserGroupsRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing describeUsers for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new DescribeUserGroupsResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<DescribeUserGroupsResponse> describeUserGroups(DescribeUserGroupsRequestData request) {
        try {
            log.info("Received describeUserGroups request: {}", request);

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            DescribeUserGroupsResponse response;
            List<UserGroup> groups = new ArrayList<>();

            int resultsRemaining = request.getMaxResults() != null ? request.getMaxResults() : defaultMaxResults;
            request.setMaxResults(resultsRemaining);

            do {
                response = userGroupService.describeUserGroups(request);
                List<UserGroup> newGroups = getAuthorizedUserGroups(response.getUserGroups(), username);

                if (newGroups.size() > resultsRemaining) {
                    groups.addAll(newGroups.subList(0, resultsRemaining));

                    NextToken nextToken = NextToken.deserialize(request.getNextToken(), UserGroupEntity.class);
                    nextToken.setPageOffset(OptionalInt.of(resultsRemaining));
                    response.setNextToken(NextToken.serialize(nextToken, UserGroupEntity.class));
                } else {
                    groups.addAll(newGroups);
                    request.setNextToken(response.getNextToken());
                }

                resultsRemaining -= newGroups.size();
            }  while (resultsRemaining > 0 && response.getNextToken() != null);

            groups = userGroupSort.getSorted(request, groups);
            response.setUserGroups(groups);

            log.info("Successfully sent describeUserGroups response of size {}", response.getUserGroups().size());
            log.debug("Full response: {}", response);

            return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, request, BAD_REQUEST_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, DESCRIBE_USER_GROUPS_DEFAULT_MESSAGE);
        }
    }

    private List<UserGroup> getAuthorizedUserGroups(List<UserGroup> groups, String username) {
        List<UserGroup> authorizedUserGroups = new ArrayList<>();
        for (UserGroup group : groups) {
            if (authorizationEngine.isAuthorized(PrincipalType.User, username, ResourceAction.viewGroupDetails, ResourceType.Group, group.getUserGroupId())) {
                log.info("User {} is authorized to view User Group {}", username, group.getUserGroupId());
                authorizedUserGroups.add(group);
            } else {
                log.warn("User {} is not authorized to view User Group {}", username, group.getUserGroupId());
            }
        }
        return authorizedUserGroups;
    }
}
