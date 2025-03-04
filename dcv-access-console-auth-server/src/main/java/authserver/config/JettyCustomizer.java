package authserver.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class JettyCustomizer implements WebServerFactoryCustomizer<JettyServletWebServerFactory> {
    @Value("${spring.security.oauth2.authorizationserver.issuer}")
    String redirectUrl;

    @Override
    public void customize(JettyServletWebServerFactory factory) {
        factory.addServerCustomizers(customize -> {
            customize.setErrorHandler(new SilentErrorHandler(redirectUrl));
        });
    }

    @AllArgsConstructor
    private static class SilentErrorHandler extends ErrorHandler {
        String redirectUrl;

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException {
            baseRequest.setHandled(true);
            response.sendRedirect(redirectUrl + "/error");
        }
    }
}
