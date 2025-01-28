// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.util.StringUtils;

@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class
})
public class DcvSmUiHandlerApp {
    private static final String[] CONFIG_NAMES = {
            "access-console-handler",
            "access-console-handler-advanced",
            "access-console-handler-secrets",
            "dependency-properties-transform"
    };

    public static void main(String[] args) {
        System.setProperty("spring.config.name", StringUtils.arrayToCommaDelimitedString(CONFIG_NAMES));
        SpringApplication.run(DcvSmUiHandlerApp.class, args);
    }
}
