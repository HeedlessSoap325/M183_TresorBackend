package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.exception.ErrorResponse;
import ch.bbw.pr.tresorbackend.exception.ApplicationError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class ApplicationErrorHandler {
    @ExceptionHandler(ApplicationError.class)
    public ResponseEntity<ErrorResponse> handleException(ApplicationError error) {
        ErrorResponse response = new ErrorResponse(
            error.getMessage(),
            error.getStatus().value(),
            Instant.now().toString()
        );

        return ResponseEntity
            .status(error.getStatus())
            .body(response);
    }
}
