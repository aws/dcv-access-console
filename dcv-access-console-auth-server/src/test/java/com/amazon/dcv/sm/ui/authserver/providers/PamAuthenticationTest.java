package com.amazon.dcv.sm.ui.authserver.providers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY;
import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazon.dcv.sm.ui.authserver.BaseTestClass;
import com.amazon.dcv.sm.ui.authserver.config.AuthorizationServerConfig;
import com.amazon.dcv.sm.ui.authserver.config.security.PamAuthenticationSecurityConfig;
import com.amazon.dcv.sm.ui.authserver.providers.pam.PamAuthenticationProvider;
import com.amazon.dcv.sm.ui.authserver.providers.pam.ProcessBuilderProvider;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

@WebMvcTest
@Import({AuthorizationServerConfig.class, PamAuthenticationSecurityConfig.class, PamAuthenticationProvider.class})
@TestPropertySource(
        properties = {
            "authentication.pam.dcvpamhelper-path=/path/to/dcvpamhelper",
            "authentication.pam.service-name=service-name",
            "authentication.pam.normalize-userid=false"
        })
public class PamAuthenticationTest extends BaseTestClass {
    private static final String REDIRECT_URI = "http://127.0.0.1:8080/login/oauth2/code/test-client-id";
    private static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";
    private static final String SPRING_SECURITY_LAST_EXCEPTION = "SPRING_SECURITY_LAST_EXCEPTION";

    private static final String TEST_USERNAME = "test-user";
    private static final String TEST_PASSWORD = "test-password";
    private static final String TEST_CLIENT_ID = "test-client-id";

    private static final String DEFAULT_AUTHORIZATION_ENDPOINT_URI = "/oauth2/authorize";
    private static final String NONCE_KEY = "nonce";
    public static final String STATE_KEY = "state";

    private static final String OPEN_ID = "openid";
    private static final String CODE = "code";
    private static final String NONCE_VALUE = "PkJQz8g3ttM0Cn-Zrdgl6snYVvEvnCPfz_pK81j3-cM";
    private static final String CODE_CHALLENGE_METHOD_VALUE = "S256";
    private static final String CODE_CHALLENGE_VALUE_ENCRYPTED = "_xkyCQ4SY0wm_2BJSfyUCgd6_KkLvzh_f2F3_D5Nm6s";
    private static final String CODE_CHALLENGE_VALUE = "FVBXonJXDUqDrMhuEv84PfCbABpVaL_TZ-jMoIWdOUo";
    public static final String STATE_VALUE = "PkJQz8g3ttM0Cn-Zrdgl6snYVvEvnCPfz_pK81j3-cM";

    private static final String LOGIN_PAGE = "http://localhost/login";
    private static final ResultMatcher LOGIN_PAGE_MATCHER = redirectedUrlPattern(LOGIN_PAGE + "*");
    private static final ResultMatcher CODE_PAGE_MATCHER =
            redirectedUrlPattern("http://127.0.0.1:8080/login/oauth2/code/test-client-id?code*");
    private static final ResultMatcher ANY_PAGE_MATCHER = redirectedUrlPattern("/*");

    @Autowired
    private transient MockMvc mvc;

    @MockBean
    private transient ProcessBuilderProvider processBuilderProvider;

    @Test
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void testRedirectToLoginPageWhenUnauthenticated() throws Exception {

        MvcResult mvcResult = mvc.perform(get(DEFAULT_AUTHORIZATION_ENDPOINT_URI)
                        .queryParam(OAuth2ParameterNames.CLIENT_ID, TEST_CLIENT_ID)
                        .queryParam(OAuth2ParameterNames.SCOPE, OPEN_ID)
                        .queryParam(OAuth2ParameterNames.REDIRECT_URI, REDIRECT_URI)
                        .queryParam(PkceParameterNames.CODE_CHALLENGE, CODE_CHALLENGE_VALUE_ENCRYPTED)
                        .queryParam(PkceParameterNames.CODE_CHALLENGE_METHOD, CODE_CHALLENGE_METHOD_VALUE)
                        .queryParam(OAuth2ParameterNames.RESPONSE_TYPE, CODE)
                        .queryParam(NONCE_KEY, NONCE_VALUE)
                        .queryParam(STATE_KEY, STATE_VALUE))
                .andExpect(status().is3xxRedirection())
                .andExpect(LOGIN_PAGE_MATCHER)
                .andReturn();

        assert mvcResult.getResponse().getRedirectedUrl() != null;

        // Ensure that the login page is displayed
        mvc.perform(get(mvcResult.getResponse().getRedirectedUrl())).andExpect(status().isOk());
    }

    private Process setupMockProcessBuilderProvider() throws IOException {
        ProcessBuilder mockProcessBuilder = mock(ProcessBuilder.class);
        Process mockProcess = mock(Process.class);
        when(processBuilderProvider.getProcessBuilder()).thenReturn(mockProcessBuilder);
        when(mockProcessBuilder.command(any(), any(), any())).thenReturn(mockProcessBuilder);
        when(mockProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)).thenReturn(mockProcessBuilder);
        when(mockProcessBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)).thenReturn(mockProcessBuilder);
        when(mockProcessBuilder.redirectErrorStream(true)).thenReturn(mockProcessBuilder);
        when(mockProcessBuilder.start()).thenReturn(mockProcess);

        // mock output process
        when(mockProcess.getOutputStream()).thenReturn(mock(OutputStream.class));

        return mockProcess;
    }

    @Test
    public void testValidPamAuthentication() throws Exception {
        // 0. Setup process builder provider to return a valid authentication
        // mock process builder
        Process mockProcess = setupMockProcessBuilderProvider();

        // set successful return value for the process
        when(mockProcess.exitValue()).thenReturn(0);

        // 1. Ensure that the login is possible
        MvcResult afterLogin = mvc.perform(post(LOGIN_PAGE)
                        .param(SPRING_SECURITY_FORM_USERNAME_KEY, TEST_USERNAME)
                        .param(SPRING_SECURITY_FORM_PASSWORD_KEY, TEST_PASSWORD)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(ANY_PAGE_MATCHER)
                .andReturn();
        MockHttpSession session = (MockHttpSession) afterLogin.getRequest().getSession(false);

        // 2. Verify that the user is logged in
        assert session != null;
        SecurityContextImpl spring_security_context =
                (SecurityContextImpl) session.getAttribute(SPRING_SECURITY_CONTEXT);
        assert spring_security_context.getAuthentication().getPrincipal().equals(TEST_USERNAME);

        // 3. Use logged in session to get the authorization code
        MvcResult mvcResult = mvc.perform(get(DEFAULT_AUTHORIZATION_ENDPOINT_URI)
                        .queryParam(OAuth2ParameterNames.CLIENT_ID, TEST_CLIENT_ID)
                        .queryParam(OAuth2ParameterNames.SCOPE, OPEN_ID)
                        .queryParam(OAuth2ParameterNames.REDIRECT_URI, REDIRECT_URI)
                        .queryParam(PkceParameterNames.CODE_CHALLENGE, CODE_CHALLENGE_VALUE)
                        .queryParam(PkceParameterNames.CODE_CHALLENGE_METHOD, CODE_CHALLENGE_METHOD_VALUE)
                        .queryParam(OAuth2ParameterNames.RESPONSE_TYPE, CODE)
                        .queryParam(NONCE_KEY, NONCE_VALUE)
                        .queryParam(STATE_KEY, STATE_VALUE)
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(CODE_PAGE_MATCHER)
                .andReturn();

        // Check the session is invalidated
        assert mvcResult.getRequest().getSession(false) == null;
    }

    @Test
    public void testMissingUsernameOrPasswordPamAuthentication() throws Exception {

        // 0. Setup process builder provider to return a valid authentication
        // mock process builder
        Process mockProcess = setupMockProcessBuilderProvider();

        // set successful return value for the process
        when(mockProcess.exitValue()).thenReturn(0);

        // 1. Ensure that the login is not possible
        MvcResult afterLogin = mvc.perform(post(LOGIN_PAGE).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(ANY_PAGE_MATCHER)
                .andReturn();
        MockHttpSession session = (MockHttpSession) afterLogin.getRequest().getSession(false);
        assert session != null;
        AuthenticationServiceException exception =
                (AuthenticationServiceException) session.getAttribute(SPRING_SECURITY_LAST_EXCEPTION);
        assert exception.getMessage().equals("Username or password is empty");
    }

    @Test
    public void testIncorrectUsernameOrPasswordPamAuthentication() throws Exception {

        // 0. Setup process builder provider to return a valid authentication
        // mock process builder
        Process mockProcess = setupMockProcessBuilderProvider();

        // set unsuccessful return value for the process
        when(mockProcess.exitValue()).thenReturn(1);

        // 1. Ensure that the login is fails
        MvcResult afterLogin = mvc.perform(post(LOGIN_PAGE)
                        .param(SPRING_SECURITY_FORM_USERNAME_KEY, TEST_USERNAME)
                        .param(SPRING_SECURITY_FORM_PASSWORD_KEY, TEST_PASSWORD)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(ANY_PAGE_MATCHER)
                .andReturn();
        MockHttpSession session = (MockHttpSession) afterLogin.getRequest().getSession(false);
        assert session != null;
        AuthenticationServiceException exception =
                (AuthenticationServiceException) session.getAttribute(SPRING_SECURITY_LAST_EXCEPTION);

        assert exception.getMessage().equals("Authentication failed");
    }
}
