package tn.esprit.abdelrahmenhamoudi4cce11.web.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tn.esprit.abdelrahmenhamoudi4cce11.exception.ResourceNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErreurReponse(int statut, String message, LocalDateTime horodatage) {
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErreurReponse handleResourceNotFound(ResourceNotFoundException ex) {
        return erreur(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErreurReponse handleIllegalArgument(IllegalArgumentException ex) {
        return erreur(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private static ErreurReponse erreur(HttpStatus statut, String message) {
        return new ErreurReponse(statut.value(), message, LocalDateTime.now(ZoneId.systemDefault()).withNano(0));
    }
}
