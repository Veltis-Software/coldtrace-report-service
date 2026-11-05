package com.acme.coldtrace.platform.shared.interfaces.rest.transform;

import com.acme.coldtrace.platform.shared.application.result.ApplicationError;
import com.acme.coldtrace.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

/**
 * Generic response assembler for application results.
 * <p>
 * It is intended for use cases that already expose {@link ApplicationError}
 * failures. Bounded-context-specific failures can keep their specialized
 * assemblers and delegate their error body creation to
 * {@link ErrorResponseAssembler}.
 *
 * @since 1.0
 */
@NullMarked
public final class ResponseEntityAssembler {
    private ResponseEntityAssembler() {
    }

    /**
     * Converts a result into a REST response.
     *
     * @param result application result
     * @param successResourceAssembler success mapper
     * @param successStatus status for a successful result
     * @param <T> success value type
     * @param <R> response resource type
     * @return response entity for success or failure
     */
    public static <T, R> ResponseEntity<?> toResponseEntityFromResult(
            Result<T, ApplicationError> result,
            Function<T, R> successResourceAssembler,
            HttpStatusCode successStatus
    ) {
        return result.fold(
                value -> new ResponseEntity<>(successResourceAssembler.apply(value), successStatus),
                ErrorResponseAssembler::toErrorResponseFromApplicationError
        );
    }
}
