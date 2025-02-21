package com.amazon.dcv.sm.ui.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.util.StringUtils;

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class DcvSmUiAuthServerApp {
    private static final String[] CONFIG_NAMES = {
        "access-console-auth-server",
        "access-console-auth-server-secrets",
        "access-console-auth-server-advanced",
        "dependency-properties-transform"
    };

    public static void main(String[] args) throws Exception {
        System.setProperty("spring.config.name", StringUtils.arrayToCommaDelimitedString(CONFIG_NAMES));

        SpringApplication.run(DcvSmUiAuthServerApp.class, args);
    }
}
