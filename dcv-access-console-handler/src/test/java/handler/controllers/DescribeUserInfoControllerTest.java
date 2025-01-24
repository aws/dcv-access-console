package handler.controllers;

import handler.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.WebContentGenerator;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DescribeUserInfoController.class)
public class DescribeUserInfoControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService mockUserService;

    @Value("${web-client-url}")
    private String origin;

    private final static String urlTemplate = "/describeUserInfo";

    private final static String testUserDisplayName = "testUserDisplayName";
    private final static String testUserRole = "testUserRole";

    @Test
    public void testDescribeCurrentUser() throws Exception {
        when(mockAuthorizationEngine.getUserDisplayName(testUser)).thenReturn(testUserDisplayName);
        when(mockAuthorizationEngine.getUserRole(testUser)).thenReturn(testUserRole);
        mvc.perform(
                get(urlTemplate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                        .header(HttpHeaders.ORIGIN, origin)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.DisplayName", is(testUserDisplayName)))
                .andExpect(jsonPath("$.Role", is(testUserRole)));
    }

    @Test
    public void testUsernameNotFoundException() throws Exception {
        when(mockAuthorizationEngine.getUserDisplayName(testUser)).thenThrow(UsernameNotFoundException.class);
        mvc.perform(
                        get(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testInternalServerException() throws Exception {
        doThrow(RuntimeException.class).when(mockUserService).updateLastLoggedInTime(testUser);
        mvc.perform(
                        get(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isInternalServerError());
    }
}
