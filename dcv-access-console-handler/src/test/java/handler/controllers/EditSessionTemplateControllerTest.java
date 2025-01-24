package handler.controllers;

import handler.authorization.enums.PrincipalType;
import handler.authorization.enums.ResourceAction;
import handler.authorization.enums.ResourceType;
import handler.exceptions.BadRequestException;
import handler.model.CreateSessionTemplateResponse;
import handler.model.OsFamily;
import handler.model.SessionTemplate;
import handler.model.Type;
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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EditSessionTemplateController.class)
public class EditSessionTemplateControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private SessionTemplateService mockSessionTemplateService;
    @Value("${web-client-url}")
    private String origin;
    private final static String urlTemplate = "/editSessionTemplate";
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

        mvc.perform(
                        put(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"TemplateId\":  \"test\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInternalServerException() throws Exception {
        when(mockSessionTemplateService.getUpdatedNameSessionTemplate(any(), any())).thenThrow(RuntimeException.class);
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, "test-user", ResourceAction.editSpecificSessionTemplate, ResourceType.SessionTemplate, "test")).thenReturn(true);
        mvc.perform(
                        put(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"TemplateId\":  \"test\", \"CreateSessionTemplateRequestData\": {}}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void editSessionTemplateSuccess() throws Exception {
        SessionTemplate sessionTemplate = new SessionTemplate().id(testString);
        when(mockSessionTemplateService.getUpdatedNameSessionTemplate(any(), any())).thenReturn(sessionTemplate);
        when(mockSessionTemplateService.saveSessionTemplate(any(), any(), anyBoolean(), any())).thenReturn(new CreateSessionTemplateResponse().sessionTemplate(sessionTemplate));
        when(mockAuthorizationEngine.isAuthorized(PrincipalType.User, "test-user", ResourceAction.editSpecificSessionTemplate, ResourceType.SessionTemplate, "test")).thenReturn(true);
        mvc.perform(
                        put(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"TemplateId\":  \"test\", \"CreateSessionTemplateRequestData\": {}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Error", nullValue()));

        sessionTemplate.setId(null);
        mvc.perform(
                        put(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{\"TemplateId\":  \"test\", \"CreateSessionTemplateRequestData\": {}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Error", nullValue()));
        verify(mockAuthorizationEngine, times(2)).getSharedListForResource(any(), any(), any(), any());
        verify(mockAuthorizationEngine, times(2)).setShareList(any(), any(), any(), any(), any());
        verify(mockSessionTemplateService, times(1)).deleteSessionTemplate(any());
        verify(mockAuthorizationEngine, times(1)).deleteResource(any(), any());
    }
}
