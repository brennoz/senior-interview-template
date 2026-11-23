package com.elemental.tech.workitems.api;

import com.elemental.tech.workitems.exception.WorkItemNotFoundException;
import com.elemental.tech.workitems.exception.WrongStatusTransitionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(WorkItemNotFoundException.class)
    ResponseEntity<ProblemDetail> onWorkItemNotFound(WorkItemNotFoundException ex) {
        log.warn("Work item has not benn found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(WrongStatusTransitionException.class)
    ResponseEntity<ProblemDetail> onInvalidTransition(WrongStatusTransitionException ex) {
        log.warn("Status transition is wrong: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> onValidationFailure(MethodArgumentNotValidException ex) {
        StringBuilder detail = new StringBuilder();
        for (var fieldError : ex.getBindingResult().getFieldErrors()) {
            if (!detail.isEmpty()) {
                detail.append("; ");
            }
            detail.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage());
        }
        String message = detail.isEmpty() ? "Validation has failed" : detail.toString();
        log.warn("Validation has failed: {}", message);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }
}
