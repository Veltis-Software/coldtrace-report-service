package com.acme.coldtrace.platform.reports.interfaces.rest.transform;

import com.acme.coldtrace.platform.aiassistance.application.model.ComplianceFindingDraft;
import com.acme.coldtrace.platform.reports.application.model.ReportAiSummary;
import com.acme.coldtrace.platform.reports.interfaces.rest.resources.ReportAiSummaryFindingResource;
import com.acme.coldtrace.platform.reports.interfaces.rest.resources.ReportAiSummaryResource;

/**
 * Assembler that converts generated report AI summaries into REST resources.
 *
 * @since 1.0
 */
public final class ReportAiSummaryResourceFromResultAssembler {
    private ReportAiSummaryResourceFromResultAssembler() {
    }

    /**
     * Converts a generated summary into a renderable REST resource.
     *
     * @param result generated report summary
     * @return REST resource with source report metrics and advisory sections
     */
    public static ReportAiSummaryResource toResourceFromResult(ReportAiSummary result) {
        var report = result.report();
        var summary = result.summary();
        return new ReportAiSummaryResource(
                report.organizationId(),
                report.id(),
                report.uuid(),
                report.type(),
                report.title(),
                result.generatedAt(),
                ReportResourceFromEntityAssembler.toResourceFromEntity(report),
                summary.executiveSummary(),
                summary.findings().stream()
                        .map(ReportAiSummaryResourceFromResultAssembler::toFindingResource)
                        .toList(),
                summary.evidenceGaps(),
                summary.recommendedActions(),
                summary.uncertaintyNotes(),
                result.modelProvider(),
                result.modelName()
        );
    }

    private static ReportAiSummaryFindingResource toFindingResource(ComplianceFindingDraft finding) {
        return new ReportAiSummaryFindingResource(
                finding.area(),
                finding.status(),
                finding.evidence(),
                finding.recommendation()
        );
    }
}
