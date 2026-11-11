package ch.bbw.pr.tresorbackend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationError extends RuntimeException {
    private final HttpStatus status;
    public ApplicationError(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
