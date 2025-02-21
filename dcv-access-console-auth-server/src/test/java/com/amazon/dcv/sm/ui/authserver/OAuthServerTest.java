package com.amazon.dcv.sm.ui.authserver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazon.dcv.sm.ui.authserver.config.AuthorizationServerConfig;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest
@Import({AuthorizationServerConfig.class})
public class OAuthServerTest extends BaseTestClass {

    private static final String REDIRECT_URI = "http://127.0.0.1:8080/login/oauth2/code/test-client-id";

    @Autowired
    private transient MockMvc mvc;

    @Test
    public void testRedirectToLoginPageWhenNotAuthorized() throws Exception {

        mvc.perform(get("/oauth2/authorize")
                        .queryParam("client_id", "test-client-id")
                        .queryParam("state", "some-state")
                        .queryParam("scope", "openid")
                        .queryParam("redirect_uri", REDIRECT_URI)
                        .queryParam("code_challenge", "_xkyCQ4SY0wm_2BJSfyUCgd6_KkLvzh_f2F3_D5Nm6s")
                        .queryParam("code_challenge_method", "S256")
                        .queryParam("response_type", "code")
                        .queryParam("nonce", "PkJQz8g3ttM0Cn-Zrdgl6snYVvEvnCPfz_pK81j3-cM"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("http://localhost/login*"));
    }

    @Test
    public void testClientErrorForNonRegisteredClient() throws Exception {

        MvcResult mvcResult = mvc.perform(get("/oauth2/authorize")
                        .queryParam("client_id", "bad-client")
                        .queryParam("state", "some-state")
                        .queryParam("scope", "openid")
                        .queryParam("redirect_uri", REDIRECT_URI)
                        .queryParam("code_challenge", "_xkyCQ4SY0wm_2BJSfyUCgd6_KkLvzh_f2F3_D5Nm6s")
                        .queryParam("code_challenge_method", "S256")
                        .queryParam("response_type", "code")
                        .queryParam("nonce", "PkJQz8g3ttM0Cn-Zrdgl6snYVvEvnCPfz_pK81j3-cM"))
                .andExpect(status().is4xxClientError())
                .andReturn();
        assert Objects.equals(
                mvcResult.getResponse().getErrorMessage(), "[invalid_request] OAuth 2.0 Parameter: client_id");
    }
}
