package authserver.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
public class RedirectController {
    @Value("${post-logout-redirect-uris}")
    String redirectUrl;

    @RequestMapping("/")
    public void redirectToWebclient(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // The Auth server doesn't have its own webpage, this currently only used for the SM UI Webclient so redirect it
        // there
        log.info("Redirecting to webclient: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
