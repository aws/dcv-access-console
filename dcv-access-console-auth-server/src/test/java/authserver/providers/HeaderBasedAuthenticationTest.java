package authserver.providers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import authserver.BaseTestClass;
import authserver.config.AuthorizationServerConfig;
import authserver.config.security.HeaderBasedAuthenticationSecurityConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest
@Import({AuthorizationServerConfig.class, HeaderBasedAuthenticationSecurityConfig.class})
@TestPropertySource(properties = "authentication-header-name=username")
public class HeaderBasedAuthenticationTest extends BaseTestClass {
    private static final String REDIRECT_URI = "http://127.0.0.1:8080/login/oauth2/code/test-client-id";

    @Autowired
    private transient MockMvc mvc;

    @Test
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void testNoHeaderNameAuthentication() throws Exception {

        MvcResult mvcResult = mvc.perform(get("/oauth2/authorize")
                        .queryParam("client_id", "test-client-id")
                        .queryParam("scope", "openid")
                        .queryParam("redirect_uri", REDIRECT_URI)
                        .queryParam("code_challenge", "_xkyCQ4SY0wm_2BJSfyUCgd6_KkLvzh_f2F3_D5Nm6s")
                        .queryParam("code_challenge_method", "S256")
                        .queryParam("response_type", "code")
                        .queryParam("nonce", "PkJQz8g3ttM0Cn-Zrdgl6snYVvEvnCPfz_pK81j3-cM")
                        .queryParam("state", "some-state"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("http://localhost/login*"))
                .andReturn();

        assert mvcResult.getResponse().getRedirectedUrl() != null;

        // Ensure that the login page is unauthorized because there is no valid header is present
        mvc.perform(get(mvcResult.getResponse().getRedirectedUrl())).andExpect(status().is4xxClientError());
    }

    @Test
    public void testHeaderNameAuthentication() throws Exception {
        String codeChallenge = "FVBXonJXDUqDrMhuEv84PfCbABpVaL_TZ-jMoIWdOUo";

        // 1. Ensure that the login is possible
        MvcResult afterLogin = mvc.perform(get("http://localhost/login").header("username", "test-user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/*"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) afterLogin.getRequest().getSession(false);

        // 2. Verify that the user is logged in
        assert session != null;
        SecurityContextImpl spring_security_context =
                (SecurityContextImpl) session.getAttribute("SPRING_SECURITY_CONTEXT");
        assert spring_security_context.getAuthentication().getPrincipal().equals("test-user");

        // 3. Use logged in session to get the authorization code
        MvcResult mvcResult = mvc.perform(get("/oauth2/authorize")
                        .queryParam("client_id", "test-client-id")
                        .queryParam("scope", "openid")
                        .queryParam("redirect_uri", REDIRECT_URI)
                        .queryParam("code_challenge", codeChallenge)
                        .queryParam("code_challenge_method", "S256")
                        .queryParam("response_type", "code")
                        .queryParam("nonce", "PkJQz8g3ttM0Cn-Zrdgl6snYVvEvnCPfz_pK81j3-cM")
                        .queryParam("state", "some-state")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("http://127.0.0.1:8080/login/oauth2/code/test-client-id?code*"))
                .andReturn();

        // 4. Check that the sesion was invalidated
        assert mvcResult.getRequest().getSession(false) == null;
    }
}
