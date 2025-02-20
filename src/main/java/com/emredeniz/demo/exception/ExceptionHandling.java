package com.emredeniz.demo.exception;

import com.emredeniz.demo.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Set;

/**
 * Global exception handler for handling various exceptions in a Spring Boot application.
 * This class provides centralized exception handling for better error management.
 */
@RestControllerAdvice
public class ExceptionHandling implements ErrorController {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandling.class);

    // Error message for unsupported HTTP methods
    private static final String METHOD_IS_NOT_ALLOWED = "This request method is not allowed on this endpoint. Please send a '%s' request";

    // Generic internal server error message
    private static final String INTERNAL_SERVER_ERROR_MSG = "An error occurred while processing the request";

    // Error message for resource not found
    private static final String RESOURCE_NOT_FOUND_MSG = "The requested resource was not found. Please check the URL and try again";

    /**
     * Handles HttpRequestMethodNotSupportedException when a request is made with an unsupported HTTP method.
     *
     * @param exception The exception thrown when an unsupported HTTP method is used.
     * @return A structured HTTP response with status 405 (Method Not Allowed).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpResponse> methodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        // Get the supported HTTP methods and ensure it's not null to avoid NullPointerException
        Set<HttpMethod> supportedMethods = exception.getSupportedHttpMethods();
        HttpMethod supportedMethod = (supportedMethods != null && !supportedMethods.isEmpty()) ? supportedMethods.iterator().next() : HttpMethod.GET;

        return createHttpResponse(HttpStatus.METHOD_NOT_ALLOWED, String.format(METHOD_IS_NOT_ALLOWED, supportedMethod));
    }

    /**
     * Handles NoResourceFoundException when a static resource or endpoint is not found.
     *
     * @param exception The exception thrown when a resource is not found.
     * @return A structured HTTP response with status 404 (Not Found).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<HttpResponse> noResourceFoundException(NoResourceFoundException exception) {
        // Log the error message for debugging
        log.error("Resource not found: {}", exception.getMessage(), exception);

        return createHttpResponse(HttpStatus.NOT_FOUND, RESOURCE_NOT_FOUND_MSG);
    }

    /**
     * Handles all generic exceptions that are not specifically caught by other handlers.
     * Logs the error message for debugging purposes.
     *
     * @param exception The thrown exception.
     * @return A structured HTTP response with status 500 (Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse> internalServerErrorException(Exception exception) {
        // Log the error message for debugging
        log.error("An unexpected error occurred: {}", exception.getMessage(), exception);

        return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG);
    }

    /**
     * Utility method to create a standardized HTTP response.
     *
     * @param httpStatus The HTTP status code to be returned.
     * @param message    The error message to be included in the response.
     * @return A ResponseEntity containing an HttpResponse object.
     */
    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(new HttpResponse(message, null, httpStatus.value()), httpStatus);
    }
}
