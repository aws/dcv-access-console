// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.controllers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.WebContentGenerator;

import handler.exceptions.BadRequestException;
import handler.model.CreateSessionTemplateResponse;
import handler.model.OsFamily;
import handler.model.SessionTemplate;
import handler.model.Type;
import handler.services.SessionTemplateService;

@WebMvcTest(CreateSessionTemplateController.class)
public class CreateSessionTemplateControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private SessionTemplateService mockSessionTemplateService;
    @Value("${web-client-url}")
    private String origin;
    private final static String urlTemplate = "/createSessionTemplate";
    private final static String testString = "test";

    @Test
    public void testBadRequest() throws Exception {
        when(mockSessionTemplateService.saveSessionTemplate(any(), any(), anyBoolean(), any())).thenThrow(BadRequestException.class);
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
        when(mockSessionTemplateService.saveSessionTemplate(any(), any(), anyBoolean(), any())).thenThrow(RuntimeException.class);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void createSessionTemplateSuccess() throws Exception {
        SessionTemplate sessionTemplate = new SessionTemplate().name(testString);
        sessionTemplate.setOsFamily(OsFamily.LINUX.getValue());
        sessionTemplate.setType(Type.VIRTUAL.getValue());
        when(mockSessionTemplateService.saveSessionTemplate(any(), any(), anyBoolean(), any())).thenReturn(new CreateSessionTemplateResponse().sessionTemplate(sessionTemplate));
        when(mockAuthorizationEngine.addSessionTemplate(any(), any())).thenReturn(true);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SessionTemplate.Name", is(testString)))
                .andExpect(jsonPath("$.SessionTemplate.OsFamily", is(OsFamily.LINUX.getValue())))
                .andExpect(jsonPath("$.SessionTemplate.Type", is(Type.VIRTUAL.getValue())))
                .andExpect(jsonPath("$.Error", nullValue()));
    }

    @Test
    public void createSessionTemplateAuthEngineFailed() throws Exception {
        SessionTemplate sessionTemplate = new SessionTemplate().name(testString);
        sessionTemplate.setOsFamily(OsFamily.LINUX.getValue());
        sessionTemplate.setType(Type.VIRTUAL.getValue());
        when(mockSessionTemplateService.saveSessionTemplate(any(), any(), anyBoolean(), any())).thenReturn(new CreateSessionTemplateResponse().sessionTemplate(sessionTemplate));

        when(mockAuthorizationEngine.addSessionTemplate(any(), any())).thenReturn(false);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.Error.Message", notNullValue()));
        verify(mockSessionTemplateService, times(1)).deleteSessionTemplate(sessionTemplate.getId());
    }
}
