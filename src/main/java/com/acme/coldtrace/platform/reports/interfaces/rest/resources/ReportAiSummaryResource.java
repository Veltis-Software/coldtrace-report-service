package com.acme.coldtrace.platform.reports.interfaces.rest.resources;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * REST resource representing an advisory AI summary for a persisted report.
 *
 * @param organizationId organization that owns the source report
 * @param reportId source report identifier
 * @param reportUuid source report business identifier
 * @param reportType source report type
 * @param reportTitle source report title
 * @param summaryGeneratedAt AI summary generation timestamp
 * @param sourceReport factual report metrics used as source of truth
 * @param executiveSummary concise advisory summary
 * @param findings structured report findings
 * @param evidenceGaps missing evidence that limits certainty
 * @param recommendedActions advisory next actions
 * @param uncertaintyNotes assumptions or missing-context notes
 * @param modelProvider configured AI provider used for this response
 * @param modelName configured AI model used for this response
 * @since 1.0
 */
public record ReportAiSummaryResource(
        Long organizationId,
        Long reportId,
        String reportUuid,
        String reportType,
        String reportTitle,
        OffsetDateTime summaryGeneratedAt,
        ReportResource sourceReport,
        String executiveSummary,
        List<ReportAiSummaryFindingResource> findings,
        List<String> evidenceGaps,
        List<String> recommendedActions,
        List<String> uncertaintyNotes,
        String modelProvider,
        String modelName
) {
}
