// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.exceptions.BadRequestException;
import handler.model.DescribeUserGroupsRequestData;
import handler.model.DescribeUserGroupsResponse;
import handler.model.UserGroup;
import handler.persistence.UserGroupEntity;
import handler.services.UserGroupService;
import handler.utils.NextToken;
import handler.utils.Sort;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.support.WebContentGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DescribeUserGroupsController.class)
public class DescribeUserGroupsControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private UserGroupService mockUserGroupService;
    @MockBean
    private Sort<DescribeUserGroupsRequestData, UserGroup> mockUserGroupSort;
    @Value("${web-client-url}")
    private String origin;
    private final static String urlTemplate = "/describeUserGroups";
    private final static String testString = "test";
    private final static String failString = "fail";

    @Test
    public void testBadRequest() throws Exception {
        when(mockUserGroupService.describeUserGroups(any())).thenThrow(BadRequestException.class);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInternalServerException() throws Exception {
        when(mockUserGroupService.describeUserGroups(any())).thenThrow(RuntimeException.class);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testDescribeGroupsSuccess() throws Exception {
        List<UserGroup> groups = new ArrayList<>();
        UserGroup user = new UserGroup().userGroupId(testString);
        UserGroup failedUser = new UserGroup().userGroupId(failString);
        groups.add(failedUser);
        groups.add(user);
        when(mockUserGroupService.describeUserGroups(any())).thenReturn(new DescribeUserGroupsResponse().userGroups(groups).nextToken(null));
        when(mockUserGroupSort.getSorted(any(), any())).thenAnswer(i -> i.getArguments()[1]);

        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.viewGroupDetails, ResourceType.Group, failString)).thenReturn(false);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.viewGroupDetails, ResourceType.Group, testString)).thenReturn(true);

        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.UserGroups", hasSize(1)))
                .andExpect(jsonPath("$.UserGroups[0].UserGroupId", is(testString)))
                .andExpect(jsonPath("$.Error", nullValue()));
    }

    @Test
    public void testDescribeGroupsWithPagination() throws Exception {
        List<UserGroup> groups = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            UserGroup user = new UserGroup().userGroupId("user" + i);
            groups.add(user);
        }

        NextToken nextToken = NextToken.builder()
                .dynamoDbStartKey(Optional.empty())
                .pageNumber(OptionalInt.of(1))
                .pageOffset(OptionalInt.of(0))
                .build();

        DescribeUserGroupsResponse firstResponse = new DescribeUserGroupsResponse().userGroups(groups.subList(0, 4)).nextToken(NextToken.serialize(nextToken, UserGroupEntity.class));
        DescribeUserGroupsResponse secondResponse = new DescribeUserGroupsResponse().userGroups(groups.subList(4, 9));
        when(mockUserGroupService.describeUserGroups(any())).thenReturn(firstResponse, secondResponse);
        when(mockUserGroupSort.getSorted(any(), any())).thenAnswer(i -> i.getArguments()[1]);

        when(mockAuthorizationEngine.isAuthorized(any(), any(), any(), any(), any())).thenReturn(true);

        MvcResult result = mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"MaxResults\": 5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.UserGroups", hasSize(5)))
                .andExpect(jsonPath("$.NextToken", notNullValue()))
                .andExpect(jsonPath("$.Error", nullValue()))
                .andReturn();

        String nextTokenString = JsonPath.read(result.getResponse().getContentAsString(), "$.NextToken");
        NextToken returnedToken = NextToken.deserialize(nextTokenString, UserGroupEntity.class);
        assertEquals(nextToken.getPageNumber(), returnedToken.getPageNumber());
        assertTrue(nextToken.getPageOffset().isPresent());
        assertTrue(returnedToken.getPageOffset().isPresent());
        assertEquals(nextToken.getPageOffset().getAsInt() + 1, returnedToken.getPageOffset().getAsInt());
    }
}
