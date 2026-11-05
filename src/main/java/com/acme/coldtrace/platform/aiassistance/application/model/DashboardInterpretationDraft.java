package com.acme.coldtrace.platform.aiassistance.application.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Structured advisory interpretation of dashboard metrics.
 *
 * @param summary concise operational summary
 * @param attentionLevel operator-facing attention level
 * @param insights metric-level insights
 * @param risks operational risks inferred from the dashboard context
 * @param recommendedActions advisory next actions
 * @param uncertaintyNotes assumptions or missing-context notes
 * @since 1.0
 */
@JsonPropertyOrder({"summary", "attentionLevel", "insights", "risks", "recommendedActions", "uncertaintyNotes"})
public record DashboardInterpretationDraft(
        @Size(max = 500) String summary,
        @Size(max = 80) String attentionLevel,
        @Size(max = 8) List<@Valid DashboardInsightDraft> insights,
        @Size(max = 8) List<@Size(max = 240) String> risks,
        @Size(max = 8) List<@Size(max = 240) String> recommendedActions,
        @Size(max = 8) List<@Size(max = 240) String> uncertaintyNotes
) {
    public DashboardInterpretationDraft {
        insights = insights == null ? List.of() : List.copyOf(insights);
        risks = risks == null ? List.of() : List.copyOf(risks);
        recommendedActions = recommendedActions == null ? List.of() : List.copyOf(recommendedActions);
        uncertaintyNotes = uncertaintyNotes == null ? List.of() : List.copyOf(uncertaintyNotes);
    }
}
