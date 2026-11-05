package com.acme.coldtrace.platform.aiassistance.domain.model.commands;

/**
 * Command to generate an advisory compliance summary from report context.
 *
 * @param reportContext persisted report context assembled by backend use cases
 * @since 1.0
 */
public record GenerateComplianceSummaryCommand(String reportContext) {
    public GenerateComplianceSummaryCommand {
        if (reportContext == null || reportContext.isBlank()) {
            throw new IllegalArgumentException("ai-assistance.compliance-summary.error.context.required");
        }
    }
}
