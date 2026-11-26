package com.acme.coldtrace.platform.reports.interfaces.rest.resources;

/**
 * REST resource representing one AI-generated report finding.
 *
 * @param area compliance or operational area referenced by the finding
 * @param status finding status
 * @param evidence factual evidence summarized from the report context
 * @param recommendation advisory recommendation
 * @since 1.0
 */
public record ReportAiSummaryFindingResource(
        String area,
        String status,
        String evidence,
        String recommendation
) {
}
