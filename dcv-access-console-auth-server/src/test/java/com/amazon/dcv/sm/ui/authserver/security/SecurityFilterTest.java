package com.amazon.dcv.sm.ui.authserver.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import com.amazon.dcv.sm.ui.authserver.BaseTestClass;
import com.amazon.dcv.sm.ui.authserver.config.AuthorizationServerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@Import({AuthorizationServerConfig.class})
public class SecurityFilterTest extends BaseTestClass {
    @Autowired
    private transient MockMvc mvc;

    @Test
    public void testCSPHeader() throws Exception {
        mvc.perform(get("/login")).andExpect(header().exists("Content-Security-Policy"));
    }
}
