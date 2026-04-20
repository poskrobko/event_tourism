package com.example.eventtourism.exception;

import com.example.eventtourism.dto.CommonDtos;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<CommonDtos.ApiMessage> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new CommonDtos.ApiMessage(ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<CommonDtos.ApiMessage> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(new CommonDtos.ApiMessage(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonDtos.ApiMessage> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(new CommonDtos.ApiMessage("Validation error"));
    }
}
