package com.acme.coldtrace.platform.shared.interfaces.rest.transform;

import com.acme.coldtrace.platform.shared.application.result.ApplicationError;
import com.acme.coldtrace.platform.shared.interfaces.rest.resources.ErrorResource;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

/**
 * Assembler for converting application errors into REST error resources.
 * <p>
 * This class centralizes the HTTP status and response-body shape used by
 * controllers while preserving the existing ColdTrace i18n message keys.
 *
 * @since 1.0
 */
@NullMarked
public final class ErrorResponseAssembler {
    private ErrorResponseAssembler() {
    }

    /**
     * Converts an application error into a response entity.
     *
     * @param error application error
     * @return response with a standard {@link ErrorResource}
     */
    public static ResponseEntity<ErrorResource> toErrorResponseFromApplicationError(ApplicationError error) {
        var status = toStatusFromErrorCode(error.code());
        var resource = new ErrorResource(error.code(), error.message(), error.details());
        return new ResponseEntity<>(resource, status);
    }

    /**
     * Converts a localized failure into a response entity.
     *
     * @param code stable application error code
     * @param status HTTP status to return
     * @param messageSource source used to resolve the message key
     * @param messageKey i18n key exposed by the application failure
     * @param args optional message interpolation arguments
     * @return response with a standard {@link ErrorResource}
     */
    public static ResponseEntity<ErrorResource> toErrorResponseFromMessageKey(
            String code,
            HttpStatusCode status,
            MessageSource messageSource,
            String messageKey,
            Object... args
    ) {
        var message = messageSource.getMessage(messageKey, args, messageKey, LocaleContextHolder.getLocale());
        return new ResponseEntity<>(new ErrorResource(code, message, messageKey), status);
    }

    /**
     * Determines the HTTP status for a stable application error code.
     *
     * @param errorCode error code
     * @return HTTP status code
     */
    public static HttpStatusCode toStatusFromErrorCode(String errorCode) {
        return switch (errorCode) {
            case "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "INVALID_CREDENTIALS" -> HttpStatus.UNAUTHORIZED;
            case "PROVIDER_VALIDATION_FAILED" -> HttpStatus.UNAUTHORIZED;
            case "SOCIAL_IDENTITY_REQUIRES_ONBOARDING" -> HttpStatusCode.valueOf(422);
            case "SOCIAL_PROVIDER_CONFIGURATION_MISSING" -> HttpStatus.SERVICE_UNAVAILABLE;
            case "BUSINESS_RULE_VIOLATION" -> HttpStatusCode.valueOf(422);
            case "UNEXPECTED_ERROR" -> HttpStatus.INTERNAL_SERVER_ERROR;
            case String code when code.endsWith("_NOT_FOUND") -> HttpStatus.NOT_FOUND;
            case String code when code.endsWith("_CONFLICT") -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * Converts a message key into an API-safe error code.
     *
     * @param messageKey ColdTrace i18n message key
     * @return stable uppercase code
     */
    public static String toErrorCodeFromMessageKey(String messageKey) {
        var lastSegment = messageKey.substring(messageKey.lastIndexOf('.') + 1);
        return lastSegment
                .replace('-', '_')
                .toUpperCase(Locale.ROOT);
    }
}
