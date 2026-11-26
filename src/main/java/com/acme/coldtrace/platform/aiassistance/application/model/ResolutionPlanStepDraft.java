package com.acme.coldtrace.platform.aiassistance.application.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * One recommended action in an advisory incident resolution plan.
 *
 * @param sequence execution order suggested by the model
 * @param action operator-facing action
 * @param rationale reason why the action is recommended
 * @param expectedOutcome expected operational result
 * @since 1.0
 */
@JsonPropertyOrder({"sequence", "action", "rationale", "expectedOutcome"})
public record ResolutionPlanStepDraft(
        @NotNull @Min(1) Integer sequence,
        @NotBlank @Size(max = 240) String action,
        @NotBlank @Size(max = 320) String rationale,
        @NotBlank @Size(max = 240) String expectedOutcome
) {
}
