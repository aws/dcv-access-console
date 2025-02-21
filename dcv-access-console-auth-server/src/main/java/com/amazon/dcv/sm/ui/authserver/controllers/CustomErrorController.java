package com.amazon.dcv.sm.ui.authserver.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
public class CustomErrorController implements ErrorController {
    @RequestMapping("/error")
    @PostMapping("/error")
    public String handleError(HttpServletRequest request) {
        log.info(
                "Error handler called with error code: {} for request: {}",
                request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE),
                request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            return switch (HttpStatus.valueOf(statusCode)) {
                case BAD_REQUEST -> "400.html";
                case UNAUTHORIZED -> "401.html";
                case FORBIDDEN -> "403.html";
                case NOT_FOUND -> "404.html";
                case INTERNAL_SERVER_ERROR -> "error.html";
                default -> "error.html";
            };
        }
        return "error.html";
    }
}
