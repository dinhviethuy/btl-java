package com.fullnestjob.common.exception;

import com.fullnestjob.common.response.ApiResponse;
import com.fullnestjob.common.response.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @Message("")
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(err -> {
                    if (err instanceof FieldError fe) {
                        return fe.getField() + ": " + fe.getDefaultMessage();
                    }
                    return err.getDefaultMessage();
                })
                .collect(Collectors.toList());
        ApiResponse<Object> res = new ApiResponse<>(errors, HttpStatus.UNPROCESSABLE_ENTITY.value(), "Validation failed", true);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(res);
    }

    @ExceptionHandler(Exception.class)
    @Message("")
    public ResponseEntity<ApiResponse<Object>> handleAny(Exception ex) {
        if (ex instanceof ResponseStatusException rse) {
            ApiResponse<Object> res = new ApiResponse<>(rse.getReason(), rse.getStatusCode().value(), "", true);
            return ResponseEntity.status(rse.getStatusCode()).body(res);
        }
        ApiResponse<Object> res = new ApiResponse<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "", true);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @Message("")
    public ResponseEntity<ApiResponse<Object>> handleMaxUpload(MaxUploadSizeExceededException ex) {
        ApiResponse<Object> res = new ApiResponse<>("Maximum upload size exceeded", HttpStatus.PAYLOAD_TOO_LARGE.value(), "", true);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(res);
    }
}


