package com.amazon.dcv.sm.ui.authserver.providers.pam;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${authentication.pam.service-name:}')")
public class PamAuthenticationProvider implements AuthenticationProvider {

    @Value("${authentication.pam.dcvpamhelper-path}")
    private String dcvPamHelperPath;

    @Value("${authentication.pam.service-name}")
    private String pamService;

    @Value("${authentication.pam.debug:false}")
    private boolean debug;

    @Value("${authentication.pam.bash:/bin/sh}")
    private String bash;

    @Value("${authentication.pam.bash-options:-c}")
    private String bashOptions;

    @Value("${authentication.pam.process-timeout:10}")
    private int processTimeout;

    @Value("${authentication.pam.normalize-userid:false}")
    private boolean normalizeUserId;

    @Value("${authentication.pam.normalize-userid-command:'id --user --name \"$0\"'}")
    private String normalizeUserIdCommand;

    @NonNull
    private ProcessBuilderProvider processBuilderProvider; // Created for testing in order to mock process builder

    private String command;

    @PostConstruct
    void init() {
        this.command = String.format(
                "exec %s %s --stdout --service=%s 3<&0", dcvPamHelperPath, debug ? "--debug" : "", pamService);
        log.info("Setting up PAM command: {} {} {}", this.bash, this.bashOptions, this.command);
    }

    private void validateCredentials(String username, String password) throws AuthenticationServiceException {
        log.info("Attempting authentication through PamAuthenticationProvider");
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new AuthenticationServiceException("Username or password is empty");
        }
        try {
            log.debug("Attempting to authenticate user: {}", username);
            Process process = processBuilderProvider
                    .getProcessBuilder()
                    .command(this.bash, this.bashOptions, this.command)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectErrorStream(true)
                    .start();
            log.debug(
                    "Process started with command: {} {} {} with pid: {}",
                    this.bash,
                    this.bashOptions,
                    this.command,
                    process.pid());

            try (OutputStream os = process.getOutputStream()) {
                log.debug("Writing username to process");
                os.write(username.getBytes(Charset.defaultCharset()));
                os.write(0);

                log.debug("Writing password to process");
                os.write(password.getBytes(Charset.defaultCharset()));
                os.write(0);
            }
            log.debug("Waiting for process with pid: {} to finish", process.pid());
            process.waitFor(processTimeout, TimeUnit.SECONDS);
            if (process.exitValue() == 0) {
                log.debug("Process with pid: {} was successful, user authenticated", process.pid());
            } else {
                log.debug("Process with pid: {} was unsuccessful, user is not authenticated", process.pid());
                throw new AuthenticationServiceException("Authentication failed");
            }

        } catch (Exception ex) {
            throw new AuthenticationServiceException("Authentication failed", ex);
        }
    }

    private String getNormalizedUserId(String principal) {
        if (!normalizeUserId) {
            log.debug("normalize-userid not enabled returning principal: {}", principal);
            return principal;
        }
        try {
            log.info("Attempting to normalize userid for: {}", principal);
            Process process = processBuilderProvider
                    .getProcessBuilder()
                    .command(this.bash, this.bashOptions, normalizeUserIdCommand, principal)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectInput(ProcessBuilder.Redirect.PIPE)
                    .redirectErrorStream(true)
                    .start();
            log.info(
                    "Process started with command: {} {} {} {} with pid: {}",
                    this.bash,
                    this.bashOptions,
                    normalizeUserIdCommand,
                    principal,
                    process.pid());

            log.info("Waiting for process with pid: {} to finish", process.pid());
            process.waitFor(processTimeout, TimeUnit.SECONDS);
            if (process.exitValue() == 0) {
                log.info("Process with pid: {} was successful, user authenticated", process.pid());
                try (InputStream inputStream = process.getInputStream()) {
                    String normalizedUserId = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
                    log.info("Principal: {} normalized to: {}", principal, normalizedUserId);
                    return normalizedUserId;
                }
            } else {
                log.info("Process with pid: {} was unsuccessful, user is not authenticated", process.pid());
                throw new AuthenticationServiceException("Normalizing userid failed");
            }
        } catch (Exception ex) {
            throw new AuthenticationServiceException("Normalizing userid failed", ex);
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getPrincipal().toString();
        String credentials = authentication.getCredentials().toString();

        // This throws an exception if there is a failure
        validateCredentials(username, credentials);

        // Return an authenticated user token
        return UsernamePasswordAuthenticationToken.authenticated(
                getNormalizedUserId((String) authentication.getPrincipal()), null, new ArrayList<>());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
