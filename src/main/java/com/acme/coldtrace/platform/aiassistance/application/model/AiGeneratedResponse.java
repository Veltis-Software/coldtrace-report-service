package com.acme.coldtrace.platform.aiassistance.application.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * AI provider metadata plus validated structured content.
 *
 * @param modelProvider configured provider used for the request
 * @param modelName configured model used for the request
 * @param content structured content returned by the model
 * @param <T> structured content type
 * @since 1.0
 */
public record AiGeneratedResponse<T>(
        @NotBlank String modelProvider,
        @NotBlank String modelName,
        @NotNull @Valid T content
) {
}
