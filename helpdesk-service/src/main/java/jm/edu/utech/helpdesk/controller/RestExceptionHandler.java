package jm.edu.utech.helpdesk.controller;
//created by rickayla mcneil 2111687
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jm.edu.utech.helpdesk.dto.ErrorResponse;
import jm.edu.utech.helpdesk.exception.DuplicateUserException;
import jm.edu.utech.helpdesk.exception.InvalidCredentialsException;
import jm.edu.utech.helpdesk.exception.InvalidStatusChangeException;
import jm.edu.utech.helpdesk.exception.NotAuthorizedException;
import jm.edu.utech.helpdesk.exception.TechnicianNotFoundException;
import jm.edu.utech.helpdesk.exception.TicketAssignmentException;
import jm.edu.utech.helpdesk.exception.TicketNotFoundException;
import jm.edu.utech.helpdesk.exception.UserNotFoundException;

// turns exceptions into JSON error responses
@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger logger = LogManager.getLogger(RestExceptionHandler.class);

    // 404 Not Found

    @ExceptionHandler({TicketNotFoundException.class, UserNotFoundException.class,
            TechnicianNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(RuntimeException ex) {
        logger.warn("REST 404: " + ex.getMessage());
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 409 Conflict

    @ExceptionHandler({DuplicateUserException.class, TicketAssignmentException.class,
            InvalidStatusChangeException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(RuntimeException ex) {
        logger.warn("REST 409: " + ex.getMessage());
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 401 Unauthorized

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidCredentials(InvalidCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // 403 Forbidden

    @ExceptionHandler(NotAuthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleNotAuthorized(NotAuthorizedException ex) {
        logger.warn("REST 403: " + ex.getMessage());
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // 400 Bad Request

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(IllegalArgumentException ex) {
        logger.warn("REST 400: " + ex.getMessage());
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // bad JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadableJson(HttpMessageNotReadableException ex) {
        logger.warn("REST 400: unreadable JSON - " + ex.getMostSpecificCause().getMessage());
        return error(HttpStatus.BAD_REQUEST,
                "The request data is missing or invalid. Check the JSON and the allowed values.");
    }

    // wrong value type in the URL
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        logger.warn("REST 400: invalid value for " + ex.getName() + " - " + ex.getValue());
        return error(HttpStatus.BAD_REQUEST, "Invalid value '" + ex.getValue() + "' for " + ex.getName() + ".");
    }

    // 500 Internal Server Error

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpected(Exception ex) {
        logger.error("REST API error - unhandled exception", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
    }

    private ErrorResponse error(HttpStatus status, String message) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message);
    }
}
