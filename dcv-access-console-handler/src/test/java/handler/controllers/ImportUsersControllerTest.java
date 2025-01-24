package handler.controllers;

import handler.exceptions.BadRequestException;
import handler.model.ImportUsersResponse;
import handler.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.WebContentGenerator;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImportUsersController.class)
public class ImportUsersControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private UserService mockUserService;
    @Value("${web-client-url}")
    private String origin;
    private final static String urlTemplate = "/importUsers";
    private final static String testString = "test";

    @Test
    public void testBadRequest() throws Exception {
        when(mockUserService.importUsers(any(), any(), any(), any(), any())).thenThrow(BadRequestException.class);
        mvc.perform(
                        post(urlTemplate)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin)
                                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInternalServerException() throws Exception {
        when(mockUserService.importUsers(any(), any(), any(), any(), any())).thenThrow(RuntimeException.class);
        MockMultipartFile mockFile = new MockMultipartFile("File", "filename.csv", "text/plain", "content".getBytes());
        mvc.perform(
                        multipart(urlTemplate)
                                .file(mockFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void importUsersSuccess() throws Exception {
        List<String> testList = List.of(testString);
        ImportUsersResponse response = new ImportUsersResponse().successfulUsersList(testList)
                .unsuccessfulUsersList(testList);
        when(mockUserService.importUsers(any(), any(), any(), any(), any())).thenReturn(response);
        MockMultipartFile mockFile = new MockMultipartFile("File", "filename.csv", "text/plain", "content".getBytes());
        mvc.perform(
                        multipart(urlTemplate)
                                .file(mockFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, WebContentGenerator.METHOD_POST)
                                .header(HttpHeaders.ORIGIN, origin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SuccessfulUsersList", hasSize(1)))
                .andExpect(jsonPath("$.SuccessfulUsersList[0]", is(testString)))
                .andExpect(jsonPath("$.UnsuccessfulUsersList", hasSize(1)))
                .andExpect(jsonPath("$.UnsuccessfulUsersList[0]", is(testString)))
                .andExpect(jsonPath("$.Error", nullValue()));
    }
}