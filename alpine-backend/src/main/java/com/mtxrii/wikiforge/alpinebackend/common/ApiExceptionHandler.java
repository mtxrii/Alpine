package com.mtxrii.wikiforge.alpinebackend.common;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mtxrii.wikiforge.alpinebackend.common.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // -------------------------------------------------------------------------
    // Application-level domain errors
    // -------------------------------------------------------------------------

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(ErrorResponse.of(ex.getStatus().value(), ex.getStatus().getReasonPhrase(), ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // Request validation errors — override ResponseEntityExceptionHandler hooks
    // -------------------------------------------------------------------------

    /** @Valid / @Validated on request bodies — returns per-field detail. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fields.putIfAbsent(err.getField(), err.getDefaultMessage()));
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.ofFields("Request validation failed", fields));
    }

    /** Malformed JSON body or unreadable request. */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(400, "Bad Request", "Request body is missing or malformed"));
    }

    /** Required query parameter is absent. */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Required parameter '" + ex.getParameterName() + "' is missing";
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(400, "Bad Request", message));
    }

    /** Path variable declared in @RequestMapping but not bound. */
    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(
            MissingPathVariableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Missing path variable: " + ex.getVariableName();
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(400, "Bad Request", message));
    }

    /** Type mismatch on path variables / query params (e.g. non-UUID where UUID expected). */
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String name = ex instanceof MethodArgumentTypeMismatchException m ? m.getName() : ex.getPropertyName();
        String message = "Invalid value for parameter '" + name + "': " + ex.getValue();
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(400, "Bad Request", message));
    }

    // -------------------------------------------------------------------------
    // HTTP-level errors — override ResponseEntityExceptionHandler hooks
    // -------------------------------------------------------------------------

    /** Route exists but not for this HTTP method. */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Method " + ex.getMethod() + " is not supported for this endpoint";
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.of(status.value(), "Method Not Allowed", message));
    }

    /** Unsupported Content-Type on the request. */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "Content-Type '" + ex.getContentType() + "' is not supported";
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.of(status.value(), "Unsupported Media Type", message));
    }

    /** No handler found for the requested path (404). */
    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = "No endpoint: " + ex.getHttpMethod() + " /" + ex.getResourcePath();
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.of(status.value(), "Not Found", message));
    }

    // -------------------------------------------------------------------------
    // Catch-all — unexpected server errors
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity
                .internalServerError()
                .body(ErrorResponse.of(500, "Internal Server Error", "An unexpected error occurred"));
    }
}
