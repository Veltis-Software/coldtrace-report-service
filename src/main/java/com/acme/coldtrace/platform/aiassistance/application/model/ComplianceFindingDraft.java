package com.acme.coldtrace.platform.aiassistance.application.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * One structured compliance or operational report finding.
 *
 * @param area compliance area referenced by the finding
 * @param status finding status
 * @param evidence evidence summarized by the model
 * @param recommendation advisory recommendation
 * @since 1.0
 */
@JsonPropertyOrder({"area", "status", "evidence", "recommendation"})
public record ComplianceFindingDraft(
        @NotBlank @Size(max = 100) String area,
        @NotBlank @Size(max = 80) String status,
        @NotBlank @Size(max = 360) String evidence,
        @NotBlank @Size(max = 320) String recommendation
) {
}
