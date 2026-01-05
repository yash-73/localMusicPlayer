package com.altspot.local.exception;

import com.altspot.local.payload.ErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //Unchecked exceptions maybe
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }

    //Resource not found exception
    @ExceptionHandler(ResourceNotFound.class)
    public ResponseEntity<ErrorDetails> resourceNotFound(RuntimeException ex) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                ex.getMessage());
            return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    //General exception for failed or unwanted conditions
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ErrorDetails> generalException(RuntimeException ex) {
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                ex.getMessage()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
