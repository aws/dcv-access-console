package handler.controllers;

import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.model.UnpublishSessionTemplateResponse;
import handler.services.SessionTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.WebContentGenerator;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UnpublishSessionTemplateController.class)
public class UnpublishSessionTemplateControllerTest extends BaseControllerTest{
    @Autowired
    private MockMvc mvc;

    @MockBean
    private SessionTemplateService mockSessionTemplateService;

    @Value("${web-client-url}")
    private String origin;

    private final static String urlTemplate = "/unpublishSessionTemplate";
    private final static String testString = "test";

    @Test
    public void testBadRequest() throws Exception {
        mvc.perform(
                        put(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUnauthorizedException() throws Exception {
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.unpublishSpecificSessionTemplate, ResourceType.SessionTemplate, testString)).thenReturn(false);
        mvc.perform(
                        put(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"Id\": \"test\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testInternalServerError() throws Exception {
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.unpublishSpecificSessionTemplate, ResourceType.SessionTemplate, testString)).thenThrow(new AuthorizationServiceException(""));
        mvc.perform(
                        put(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"Id\": \"test\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testUnpublishSessionTemplateSuccess() throws Exception {
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, testUser, ResourceAction.unpublishSpecificSessionTemplate, ResourceType.SessionTemplate, testString)).thenReturn(true);
        mvc.perform(
                        put(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"Id\": \"test\"}"))
                .andExpect(status().isOk());
    }
}
