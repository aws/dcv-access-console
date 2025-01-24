package handler.controllers;

import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.authorization.enums.ShareLevel;
import handler.services.SessionTemplateService;
import handler.services.UserGroupService;
import handler.model.UserGroup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.WebContentGenerator;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CreateUserGroupController.class)
public class CreateUserGroupControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserGroupService mockUserGroupService;
    @MockBean
    private SessionTemplateService mockSessionTemplateService;
    @Value("${web-client-url}")
    private String origin;

    private final static String urlTemplate = "/createUserGroup";

    private final static String groupId = "group1";
    private final static String user1Id = "user1";
    private final static String user2Id = "user2";
    private final static String sessionTemplate1Id = "sessionTemplate1";
    private final static String sessionTemplate2Id = "sessionTemplate2";
    private final static String sessionTemplate3Id = "sessionTemplate3";

    private final static String requestBody = "{\"UserGroupId\": \"group1\"}";
    private final static String requestBodyWithUserIds = "{" +
            "\"UserGroupId\": \"group1\"," +
            "\"UserIds\": [\"user1\", \"user2\"]" +
        "}";
    private final static String requestBodyWithSessionTemplates = "{" +
            "\"UserGroupId\": \"group1\"," +
            "\"SessionTemplateIds\": [\"sessionTemplate1\", \"sessionTemplate2\", \"sessionTemplate3\"]" +
            "}";

    @Test
    public void testConflict() throws Exception {
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    public void testBadRequest() throws Exception {
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInternalServerError() throws Exception {
        when(mockUserGroupService.createUserGroup(groupId, null, false)).thenReturn(new UserGroup().userGroupId(groupId));
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content(requestBody))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testSuccessfulRequestNoSessionTemplates() throws Exception {
        when(mockUserGroupService.createUserGroup(groupId, null, false)).thenReturn(new UserGroup().userGroupId(groupId));
        when(mockAuthorizationEngine.addGroup(groupId)).thenReturn(true);

        when(mockUserGroupService.addUserToGroup(user1Id, groupId)).thenReturn(true);
        when(mockUserGroupService.addUserToGroup(user2Id, groupId)).thenReturn(false);

        when(mockAuthorizationEngine.addUserToGroup(user1Id, groupId)).thenReturn(true);

        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content(requestBodyWithUserIds))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SuccessfulUsersList", notNullValue()))
                .andExpect(jsonPath("$.SuccessfulUsersList", hasSize(1)))
                .andExpect(jsonPath("$.SuccessfulUsersList[0]", is(user1Id)))
                .andExpect(jsonPath("$.UnsuccessfulUsersList", notNullValue()))
                .andExpect(jsonPath("$.UnsuccessfulUsersList", hasSize(1)))
                .andExpect(jsonPath("$.UnsuccessfulUsersList[0]", is(user2Id)))
                .andExpect(jsonPath("$.SuccessfulSessionTemplateList", nullValue()))
                .andExpect(jsonPath("$.UnsuccessfulSessionTemplateList", nullValue()));
    }

    @Test
    public void testSuccessfulRequestNoUsers() throws Exception {
        when(mockUserGroupService.createUserGroup(groupId, null, false)).thenReturn(new UserGroup().userGroupId(groupId));
        when(mockAuthorizationEngine.addGroup(groupId)).thenReturn(true);

        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.viewSessionTemplateDetails,
                ResourceType.SessionTemplate, sessionTemplate1Id)).thenReturn(true);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.viewSessionTemplateDetails,
                ResourceType.SessionTemplate, sessionTemplate2Id)).thenReturn(true);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.viewSessionTemplateDetails,
                ResourceType.SessionTemplate, sessionTemplate3Id)).thenReturn(false);

        when(mockAuthorizationEngine.addPrincipalToSharedList(PrincipalType.Group, groupId, ResourceType.SessionTemplate,
                sessionTemplate1Id, ShareLevel.publishedTo)).thenReturn(true);
        when(mockAuthorizationEngine.addPrincipalToSharedList(PrincipalType.Group, groupId, ResourceType.SessionTemplate,
                sessionTemplate2Id, ShareLevel.publishedTo)).thenReturn(false);

        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content(requestBodyWithSessionTemplates))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SuccessfulSessionTemplateList", notNullValue()))
                .andExpect(jsonPath("$.SuccessfulSessionTemplateList", hasSize(1)))
                .andExpect(jsonPath("$.SuccessfulSessionTemplateList[0]", is(sessionTemplate1Id)))
                .andExpect(jsonPath("$.UnsuccessfulSessionTemplateList", notNullValue()))
                .andExpect(jsonPath("$.UnsuccessfulSessionTemplateList", hasSize(2)))
                .andExpect(jsonPath("$.UnsuccessfulSessionTemplateList[0]", is(sessionTemplate3Id)))
                .andExpect(jsonPath("$.UnsuccessfulSessionTemplateList[1]", is(sessionTemplate2Id)))
                .andExpect(jsonPath("$.SuccessfulUsersList", nullValue()))
                .andExpect(jsonPath("$.UnsuccessfulUsersList", nullValue()));
    }

}
