// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import handler.authorization.engines.entities.SetShareListResponse;
import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.ShareLevel;
import handler.services.SessionTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.WebContentGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeleteSessionTemplatesController.class)
public class DeleteSessionTemplateControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private SessionTemplateService mockSessionTemplateService;
    @Value("${web-client-url}")
    private String origin;
    private final static String urlTemplate = "/deleteSessionTemplates";
    private final static String testString = "test";

    @Test
    public void testBadRequest() throws Exception {
        mvc.perform(
                        delete(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"Ids\":  null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInternalServerException() throws Exception {
        doThrow(RuntimeException.class).when(mockSessionTemplateService).deleteSessionTemplates(any());
        mvc.perform(
                        delete(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"Ids\": [\"test\"]}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void deleteSessionTemplateSuccess() throws Exception {
        List<String> emptyList = new ArrayList<>();
        SetShareListResponse setShareListResponse = SetShareListResponse.builder().unSuccessfulUsers(new ArrayList<>()).unSuccessfulGroups(new ArrayList<>()).build();
        SetShareListResponse unsuccessfulUsersResponse = SetShareListResponse.builder().unSuccessfulUsers(List.of(testString)).unSuccessfulGroups(new ArrayList<>()).build();
        SetShareListResponse unsuccessfulGroupsResponse = SetShareListResponse.builder().unSuccessfulUsers(new ArrayList<>()).unSuccessfulGroups(List.of(testString)).build();
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.deleteSessionTemplate, ResourceType.SessionTemplate, testString)).thenReturn(true);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.deleteSessionTemplate, ResourceType.SessionTemplate, "fail")).thenReturn(true);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.deleteSessionTemplate, ResourceType.SessionTemplate, "another")).thenReturn(true);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.deleteSessionTemplate, ResourceType.SessionTemplate, "anotherOne")).thenReturn(true);
        when(mockAuthorizationEngine.setShareList(emptyList, emptyList,ResourceType.SessionTemplate, testString, ShareLevel.publishedTo)).thenReturn(setShareListResponse);
        when(mockAuthorizationEngine.setShareList(emptyList, emptyList,ResourceType.SessionTemplate, "another", ShareLevel.publishedTo)).thenReturn(unsuccessfulUsersResponse);
        when(mockAuthorizationEngine.setShareList(emptyList, emptyList,ResourceType.SessionTemplate, "anotherOne", ShareLevel.publishedTo)).thenReturn(unsuccessfulGroupsResponse);
        when(mockAuthorizationEngine.setShareList(emptyList, emptyList,ResourceType.SessionTemplate, "fail", ShareLevel.publishedTo)).thenThrow(RuntimeException.class);
        mvc.perform(
                        delete(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"Ids\": [\"test\", \"fail\", \"another\", \"anotherOne\", null]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SuccessfulList", hasSize(1)))
                .andExpect(jsonPath("$.UnsuccessfulList", hasSize(4)))
                .andExpect(jsonPath("$.SuccessfulList[0]", is(testString)))
                .andExpect(jsonPath("$.UnsuccessfulList[0]", is("fail")))
                .andExpect(jsonPath("$.UnsuccessfulList[1]", is("another")))
                .andExpect(jsonPath("$.UnsuccessfulList[2]", is("anotherOne")))
                .andExpect(jsonPath("$.UnsuccessfulList[3]", nullValue()))
                .andExpect(jsonPath("$.Error", nullValue()));
    }
}
