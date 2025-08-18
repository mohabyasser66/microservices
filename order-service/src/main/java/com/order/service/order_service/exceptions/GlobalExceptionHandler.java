// package com.order.service.order_service.exceptions;

// import lombok.extern.slf4j.Slf4j;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.ControllerAdvice;
// import org.springframework.web.bind.annotation.ExceptionHandler;
// import org.springframework.web.context.request.WebRequest;
// import org.springframework.web.reactive.function.client.WebClientResponseException;

// import java.time.LocalDateTime;
// import java.util.HashMap;
// import java.util.Map;

// @ControllerAdvice
// @Slf4j
// public class GlobalExceptionHandler {

//     @ExceptionHandler(IllegalArgumentException.class)
//     public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
//             IllegalArgumentException ex, WebRequest request) {
//         log.error("IllegalArgumentException: {}", ex.getMessage());

//         Map<String, Object> errorDetails = new HashMap<>();
//         errorDetails.put("timestamp", LocalDateTime.now());
//         errorDetails.put("message", ex.getMessage());
//         errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
//         errorDetails.put("error", "Bad Request");

//         return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
//     }

//     @ExceptionHandler(WebClientResponseException.class)
//     public ResponseEntity<Map<String, Object>> handleWebClientResponseException(
//             WebClientResponseException ex, WebRequest request) {
//         log.error("WebClientResponseException (HTTP {}): {}", ex.getStatusCode(), ex.getMessage());

//         Map<String, Object> errorDetails = new HashMap<>();
//         errorDetails.put("timestamp", LocalDateTime.now());

//         if (ex.getStatusCode().is4xxClientError()) {
//             errorDetails.put("message", "Invalid request: " + ex.getMessage());
//             errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
//             errorDetails.put("error", "Bad Request");
//             return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
//         } else {
//             errorDetails.put("message", "Service temporarily unavailable. Please try again later.");
//             errorDetails.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
//             errorDetails.put("error", "Service Unavailable");
//             return new ResponseEntity<>(errorDetails, HttpStatus.SERVICE_UNAVAILABLE);
//         }
//     }

//     @ExceptionHandler(RuntimeException.class)
//     public ResponseEntity<Map<String, Object>> handleRuntimeException(
//             RuntimeException ex, WebRequest request) {
//         log.error("RuntimeException: {}", ex.getMessage());

//         Map<String, Object> errorDetails = new HashMap<>();
//         errorDetails.put("timestamp", LocalDateTime.now());
//         errorDetails.put("message", ex.getMessage());
//         errorDetails.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
//         errorDetails.put("error", "Internal Server Error");

//         return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
//     }

//     @ExceptionHandler(Exception.class)
//     public ResponseEntity<Map<String, Object>> handleGenericException(
//             Exception ex, WebRequest request) {
//         log.error("Generic Exception: {}", ex.getMessage(), ex);

//         Map<String, Object> errorDetails = new HashMap<>();
//         errorDetails.put("timestamp", LocalDateTime.now());
//         errorDetails.put("message", "An unexpected error occurred");
//         errorDetails.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
//         errorDetails.put("error", "Internal Server Error");

//         return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
//     }
// }