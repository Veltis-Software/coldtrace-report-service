package com.acme.coldtrace.platform.reports.domain.model.commands;

/**
 * Command for generating an advisory AI summary for a persisted report.
 *
 * @param organizationId organization identifier from the route
 * @param reportId report identifier from the route
 * @since 1.0
 */
public record GenerateReportAiSummaryCommand(Long organizationId, Long reportId) {
    /**
     * Validates route identifiers.
     *
     * @throws IllegalArgumentException when an identifier is missing or invalid
     */
    public GenerateReportAiSummaryCommand {
        organizationId = requirePositive(organizationId, "reports.report.error.organizationId.invalid");
        reportId = requirePositive(reportId, "reports.report.error.reportId.invalid");
    }

    private static Long requirePositive(Long value, String messageKey) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(messageKey);
        }
        return value;
    }
}
