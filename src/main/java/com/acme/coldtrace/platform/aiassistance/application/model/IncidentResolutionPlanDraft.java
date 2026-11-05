package com.acme.coldtrace.platform.aiassistance.application.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Structured advisory plan for an incident. It does not mutate incident state.
 *
 * @param summary concise reasoning summary for operator review
 * @param probableCause probable operational cause inferred from backend context
 * @param recommendedSteps ordered advisory steps
 * @param correctiveActionDraft draft corrective action text
 * @param resolutionNotesDraft draft resolution notes text
 * @param escalationRecommendation advisory escalation recommendation
 * @param requiredEvidence evidence an operator should collect before approval
 * @param uncertaintyNotes assumptions or missing-context notes
 * @since 1.0
 */
@JsonPropertyOrder({
        "summary",
        "probableCause",
        "recommendedSteps",
        "correctiveActionDraft",
        "resolutionNotesDraft",
        "escalationRecommendation",
        "requiredEvidence",
        "uncertaintyNotes"
})
public record IncidentResolutionPlanDraft(
        @NotBlank @Size(max = 400) String summary,
        @NotBlank @Size(max = 320) String probableCause,
        @NotEmpty @Size(max = 8) List<@Valid ResolutionPlanStepDraft> recommendedSteps,
        @NotBlank @Size(max = 600) String correctiveActionDraft,
        @NotBlank @Size(max = 600) String resolutionNotesDraft,
        @NotNull @Valid EscalationRecommendationDraft escalationRecommendation,
        @NotEmpty @Size(max = 8) List<@NotBlank @Size(max = 200) String> requiredEvidence,
        @NotEmpty @Size(max = 8) List<@NotBlank @Size(max = 240) String> uncertaintyNotes
) {
    public IncidentResolutionPlanDraft {
        recommendedSteps = recommendedSteps == null ? List.of() : List.copyOf(recommendedSteps);
        requiredEvidence = requiredEvidence == null ? List.of() : List.copyOf(requiredEvidence);
        uncertaintyNotes = uncertaintyNotes == null ? List.of() : List.copyOf(uncertaintyNotes);
    }
}
