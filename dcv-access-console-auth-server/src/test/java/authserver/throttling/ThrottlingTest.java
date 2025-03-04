package authserver.throttling;

import static authserver.throttling.ThrottlingFilter.THROTTLE_MESSAGE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import authserver.BaseTestClass;
import authserver.config.AuthorizationServerConfig;
import authserver.config.security.PamAuthenticationSecurityConfig;
import authserver.providers.pam.PamAuthenticationProvider;
import authserver.providers.pam.ProcessBuilderProvider;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@Import({AuthorizationServerConfig.class, PamAuthenticationSecurityConfig.class, PamAuthenticationProvider.class})
@TestPropertySource(
        properties = {
            "authentication.pam.dcvpamhelper-path=/path/to/dcvpamhelper",
            "authentication.pam.service-name=service-name"
        })
public class ThrottlingTest extends BaseTestClass {
    private static final String REDIRECT_URI = "http://127.0.0.1:8080/login/oauth2/code/test-client-id";

    @Autowired
    private transient MockMvc mvc;

    @MockBean
    private transient ProcessBuilderProvider processBuilderProvider;

    @Test
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void testThrottledRequest() throws Exception {
        when(this.mockProbe.isConsumed()).thenReturn(false);

        // The call will fail due to throttling
        mvc.perform(get("/oauth2/authorize")
                        .queryParam("client_id", "test-client-id")
                        .queryParam("scope", "openid")
                        .queryParam("redirect_uri", REDIRECT_URI)
                        .queryParam("code_challenge", "_xkyCQ4SY0wm_2BJSfyUCgd6_KkLvzh_f2F3_D5Nm6s")
                        .queryParam("code_challenge_method", "S256")
                        .queryParam("response_type", "code")
                        .queryParam("nonce", "PkJQz8g3ttM0Cn-Zrdgl6snYVvEvnCPfz_pK81j3-cM")
                        .queryParam("state", "some-state"))
                .andExpect(status().isTooManyRequests())
                .andExpect(status().reason(THROTTLE_MESSAGE));
    }

    @Test
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void testLoginThrottledRequest() throws Exception {
        when(mockProbe.isConsumed()).thenReturn(false);

        // The call will fail due to throttling
        mvc.perform(get("/login")).andExpect(status().isTooManyRequests()).andExpect(status().reason(THROTTLE_MESSAGE));
    }
}
