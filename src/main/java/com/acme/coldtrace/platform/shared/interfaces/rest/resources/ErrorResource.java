package com.acme.coldtrace.platform.shared.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resource returned when a REST request cannot be completed.
 *
 * @param code stable application error code
 * @param message localized error message when available
 * @param details additional bounded-context-specific details
 * @since 1.0
 */
@Schema(
        name = "ErrorResponse",
        description = "Standard error response returned by ColdTrace REST endpoints"
)
public record ErrorResource(
        @Schema(description = "Stable application error code", example = "ASSET_NOT_FOUND")
        String code,

        @Schema(description = "Human-readable localized error message", example = "Asset was not found")
        String message,

        @Schema(description = "Additional error details or request context", example = "asset-management.asset.error.asset-not-found")
        String details
) {
}
