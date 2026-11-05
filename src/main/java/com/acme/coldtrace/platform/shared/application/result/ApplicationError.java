package com.acme.coldtrace.platform.shared.application.result;

import org.jspecify.annotations.NullMarked;

/**
 * Application-layer error representation shared by REST assemblers.
 * <p>
 * The record keeps a stable error code, a default human-readable message, and
 * details with the bounded-context-specific message key or contextual data
 * needed by the interface layer.
 *
 * @param code stable error code used by clients and response mappers
 * @param message default non-localized error message
 * @param details bounded-context-specific details or i18n message key
 * @since 1.0
 */
@NullMarked
public record ApplicationError(String code, String message, String details) {
    /**
     * Builds a validation error.
     *
     * @param field field, argument or request area that failed validation
     * @param details validation details
     * @return application error with {@code VALIDATION_ERROR} code
     */
    public static ApplicationError validationError(String field, String details) {
        return new ApplicationError("VALIDATION_ERROR", "Request validation failed", "%s: %s".formatted(field, details));
    }

    /**
     * Builds an authentication failure for invalid credentials.
     *
     * @param details bounded-context-specific details or i18n message key
     * @return application error with {@code INVALID_CREDENTIALS} code
     */
    public static ApplicationError invalidCredentials(String details) {
        return new ApplicationError("INVALID_CREDENTIALS", "Invalid email or password", details);
    }

    /**
     * Builds a provider authentication failure.
     *
     * @param details bounded-context-specific details or i18n message key
     * @return application error with {@code PROVIDER_VALIDATION_FAILED} code
     */
    public static ApplicationError providerValidationFailed(String details) {
        return new ApplicationError("PROVIDER_VALIDATION_FAILED", "Provider validation failed", details);
    }

    /**
     * Builds a social onboarding-required failure.
     *
     * @param details bounded-context-specific details or i18n message key
     * @return application error with {@code SOCIAL_IDENTITY_REQUIRES_ONBOARDING} code
     */
    public static ApplicationError socialIdentityRequiresOnboarding(String details) {
        return new ApplicationError(
                "SOCIAL_IDENTITY_REQUIRES_ONBOARDING",
                "Social identity requires onboarding",
                details
        );
    }

    /**
     * Builds a social provider configuration failure.
     *
     * @param details bounded-context-specific details or i18n message key
     * @return application error with {@code SOCIAL_PROVIDER_CONFIGURATION_MISSING} code
     */
    public static ApplicationError socialProviderConfigurationMissing(String details) {
        return new ApplicationError(
                "SOCIAL_PROVIDER_CONFIGURATION_MISSING",
                "Social provider configuration is missing",
                details
        );
    }

    /**
     * Builds a not-found error for a resource.
     *
     * @param resourceName resource name
     * @param identifier resource identifier
     * @return application error with a resource-specific not-found code
     */
    public static ApplicationError notFound(String resourceName, String identifier) {
        var code = "%s_NOT_FOUND".formatted(toErrorCodePrefix(resourceName));
        return new ApplicationError(code, "%s was not found".formatted(resourceName), identifier);
    }

    /**
     * Builds a conflict error for a resource.
     *
     * @param resourceName resource name
     * @param details conflict details
     * @return application error with a resource-specific conflict code
     */
    public static ApplicationError conflict(String resourceName, String details) {
        var code = "%s_CONFLICT".formatted(toErrorCodePrefix(resourceName));
        return new ApplicationError(code, "%s conflict".formatted(resourceName), details);
    }

    /**
     * Builds a business-rule violation error.
     *
     * @param details business-rule details
     * @return application error with {@code BUSINESS_RULE_VIOLATION} code
     */
    public static ApplicationError businessRuleViolation(String details) {
        return new ApplicationError("BUSINESS_RULE_VIOLATION", "Business rule violation", details);
    }

    private static String toErrorCodePrefix(String resourceName) {
        return resourceName
                .trim()
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase();
    }
}
