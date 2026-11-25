package com.acme.coldtrace.platform.aiassistance.domain.model.commands;

/**
 * Command to generate an advisory dashboard interpretation.
 *
 * @param dashboardContext persisted dashboard metrics assembled by backend use cases
 * @param responseLanguage operator-facing language requested for generated text
 * @since 1.0
 */
public record GenerateDashboardInterpretationCommand(String dashboardContext, String responseLanguage) {
    public GenerateDashboardInterpretationCommand {
        if (dashboardContext == null || dashboardContext.isBlank()) {
            throw new IllegalArgumentException("ai-assistance.dashboard-interpretation.error.context.required");
        }
        responseLanguage = responseLanguage == null || responseLanguage.isBlank()
                ? "English"
                : responseLanguage.trim();
    }
}
