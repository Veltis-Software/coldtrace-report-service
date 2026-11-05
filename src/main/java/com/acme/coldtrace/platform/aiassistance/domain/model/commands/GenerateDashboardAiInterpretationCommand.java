package com.acme.coldtrace.platform.aiassistance.domain.model.commands;

/**
 * Command for generating an advisory AI interpretation of the organization dashboard.
 *
 * @param organizationId organization identifier from the route
 * @param question optional operator question
 * @param preferredLanguage optional requested response language
 * @param acceptLanguageHeader optional HTTP Accept-Language header
 * @since 1.0
 */
public record GenerateDashboardAiInterpretationCommand(
        Long organizationId,
        String question,
        String preferredLanguage,
        String acceptLanguageHeader
) {
    private static final int QUESTION_MAX_LENGTH = 240;
    private static final int LANGUAGE_PREFERENCE_MAX_LENGTH = 128;

    /**
     * Validates route and request values.
     *
     * @throws IllegalArgumentException when organization id or question is invalid
     */
    public GenerateDashboardAiInterpretationCommand {
        organizationId = requirePositive(
                organizationId,
                "ai-assistance.dashboard-interpretation.error.organizationId.invalid"
        );
        question = normalizeQuestion(question);
        preferredLanguage = normalizeLanguagePreference(preferredLanguage);
        acceptLanguageHeader = normalizeLanguagePreference(acceptLanguageHeader);
    }

    private static Long requirePositive(Long value, String messageKey) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(messageKey);
        }
        return value;
    }

    private static String normalizeQuestion(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        var normalized = value.trim();
        if (normalized.length() > QUESTION_MAX_LENGTH) {
            throw new IllegalArgumentException("ai-assistance.dashboard-interpretation.error.question.invalid");
        }
        return normalized;
    }

    private static String normalizeLanguagePreference(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        var normalized = value.trim();
        if (normalized.length() > LANGUAGE_PREFERENCE_MAX_LENGTH) {
            return normalized.substring(0, LANGUAGE_PREFERENCE_MAX_LENGTH);
        }
        return normalized;
    }
}
