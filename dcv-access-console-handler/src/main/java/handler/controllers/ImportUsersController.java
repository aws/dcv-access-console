package handler.controllers;

import handler.api.ImportUsersApi;
import handler.authorization.engines.AbstractAuthorizationEngine;
import handler.errors.HandlerErrorMessage;
import handler.exceptions.BadRequestException;
import handler.model.Error;
import handler.model.ImportUsersResponse;
import handler.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static handler.errors.CommonErrorsEnum.BAD_REQUEST_ERROR;
import static handler.errors.ImportUsersErrors.IMPORT_USERS_DEFAULT_MESSAGE;

@Slf4j
@RestController
@AllArgsConstructor
public class ImportUsersController implements ImportUsersApi {
    private UserService userService;
    private final AbstractAuthorizationEngine authorizationEngine;

    private ResponseEntity<ImportUsersResponse> sendExceptionResponse(HttpStatus status, Exception e, Boolean overwriteExistingUsers, Boolean overwriteGroups, HandlerErrorMessage errorMessage) {
        log.error("Error while performing importUsers with overwriteExistingUsers={} overwriteGroups={}", overwriteExistingUsers, overwriteGroups, e);
        Error error = new Error().code(String.valueOf(status.value())).message(errorMessage.getDescription());
        return new ResponseEntity<>(new ImportUsersResponse().error(error), status);
    }

    @Override
    @CrossOrigin("${web-client-url}")
    public ResponseEntity<ImportUsersResponse> importUsers(MultipartFile file, Boolean overwriteExistingUsers, Boolean overwriteGroups) {
        try {
            log.info("Received importUsers request: overwriteExistingUsers={} overwriteGroups={}", overwriteExistingUsers, overwriteGroups);

            ImportUsersResponse response = userService.importUsers(file, overwriteExistingUsers, overwriteGroups, authorizationEngine.getRoles(), authorizationEngine.getDefaultUserRole());
            authorizationEngine.loadEntities();

            log.info("Successfully sent importUsers response: {}", response);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadRequestException e) {
            return sendExceptionResponse(HttpStatus.BAD_REQUEST, e, overwriteExistingUsers, overwriteGroups, BAD_REQUEST_ERROR);
        } catch (Exception e) {
            return sendExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, e, overwriteExistingUsers, overwriteGroups, IMPORT_USERS_DEFAULT_MESSAGE);
        }
    }
}
