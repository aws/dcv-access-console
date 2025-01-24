package handler.controllers;

import handler.api.ValidateSessionTemplateApi;
import handler.brokerclients.BrokerClient;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.model.ValidateSessionTemplateRequestData;
import handler.model.Error;
import handler.model.ValidateSessionTemplateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

import static handler.errors.ValidateSessionTemplateErrors.VALIDATE_SESSION_TEMPLATE_DEFAULT_MESSAGE;

@AllArgsConstructor
@RestController
@Slf4j
public class ValidateSessionTemplateController implements ValidateSessionTemplateApi {
    private final BrokerClient brokerClient;
    private ObjectMapper objectMapper;

    private ResponseEntity<ValidateSessionTemplateResponse> sendExceptionResponse(HttpStatus status, Exception e, ValidateSessionTemplateRequestData request, HandlerErrorMessage errorMessage) {
        log.error("Error while performing validateSessionTemplate for {}", request, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new ValidateSessionTemplateResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<ValidateSessionTemplateResponse> validateSessionTemplate(ValidateSessionTemplateRequestData request) {
        try {
            log.info("Received validateSessionTemplate request: {}", request);
            ValidateSessionTemplateResponse response = new ValidateSessionTemplateResponse();
            try {
                brokerClient.validateSessionTemplate(request.getCreateSessionTemplateRequestData(), request.getIgnoreExisting());
            }
            catch (BadRequestException e) {
                log.debug(e.getMessage());
                response.failureReasons(objectMapper.readValue(e.getMessage(), Map.class));
            }
            log.info("Successfully sent validateSessionTemplate response: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, request, VALIDATE_SESSION_TEMPLATE_DEFAULT_MESSAGE);
        }
    }
}
