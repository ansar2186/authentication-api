package com.ansari.authentication_api.exception;

import com.ansari.authentication_api.Io.ApiErrorResponse;
import com.ansari.authentication_api.Io.FieldErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalException {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> new FieldErrorDetail(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .errors(errors)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(HttpStatus status, String error, String field, String message, HttpServletRequest request) {
        List<FieldErrorDetail> errors = List.of(new FieldErrorDetail(field, message));

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .errors(errors)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(UserAlreadyExist.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyExists(UserAlreadyExist ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), "email", ex.getMessage(), request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "email", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidOtpException(InvalidOtpException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "email", ex.getMessage(), request);
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handleOtpExpiredException(OtpExpiredException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "email", ex.getMessage(), request);
    }

}
