package com.acme.coldtrace.platform.aiassistance.domain.model.commands;

/**
 * Command to generate an advisory incident resolution plan from backend context.
 *
 * @param incidentContext persisted incident context assembled by backend use cases
 * @since 1.0
 */
public record GenerateIncidentResolutionPlanDraftCommand(String incidentContext) {
    public GenerateIncidentResolutionPlanDraftCommand {
        if (incidentContext == null || incidentContext.isBlank()) {
            throw new IllegalArgumentException("ai-assistance.incident-plan.error.context.required");
        }
    }
}
