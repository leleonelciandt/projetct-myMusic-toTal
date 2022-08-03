package com.ciandt.summit.bootcamp2022.common.exception;

import com.ciandt.summit.bootcamp2022.common.exception.dto.ValidationErrorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestControllerAdvice
public class ExceptionHandlerConstraints {
    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ValidationErrorDto handle(MethodArgumentNotValidException exception, HttpServletRequest httpRequest) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        List<String> errors = new ArrayList<>();
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        fieldErrors.forEach(e -> {

            String message = messageSource.getMessage(e, Locale.US);
            String error = "Field: " + e.getField() + " -  Error: " + message;
            errors.add(error);

        });

        return new ValidationErrorDto(timestamp, 400, HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation of body request failed.", errors, httpRequest.getServletPath());
    }
}
