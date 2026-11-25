package com.acme.coldtrace.platform.aiassistance.application.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Structured advisory compliance summary generated from backend report context.
 *
 * @param executiveSummary concise compliance summary
 * @param findings structured report findings
 * @param evidenceGaps missing evidence that blocks stronger conclusions
 * @param recommendedActions advisory next actions
 * @param uncertaintyNotes assumptions or missing-context notes
 * @since 1.0
 */
@JsonPropertyOrder({"executiveSummary", "findings", "evidenceGaps", "recommendedActions", "uncertaintyNotes"})
public record ComplianceSummaryDraft(
        @NotBlank @Size(max = 600) String executiveSummary,
        @Size(max = 10) List<ComplianceFindingDraft> findings,
        @Size(max = 8) List<String> evidenceGaps,
        @Size(max = 8) List<String> recommendedActions,
        @Size(max = 8) List<String> uncertaintyNotes
) {
    public ComplianceSummaryDraft {
        findings = findings == null ? List.of() : List.copyOf(findings);
        evidenceGaps = evidenceGaps == null ? List.of() : List.copyOf(evidenceGaps);
        recommendedActions = recommendedActions == null ? List.of() : List.copyOf(recommendedActions);
        uncertaintyNotes = uncertaintyNotes == null ? List.of() : List.copyOf(uncertaintyNotes);
    }
}
