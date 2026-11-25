package com.acme.coldtrace.platform.aiassistance.application.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.Size;

/**
 * One structured insight for dashboard interpretation.
 *
 * @param title short insight title
 * @param metric metric or operational area referenced by the insight
 * @param interpretation model interpretation
 * @param severity business severity for operators
 * @since 1.0
 */
@JsonPropertyOrder({"title", "metric", "interpretation", "severity"})
public record DashboardInsightDraft(
        @Size(max = 100) String title,
        @Size(max = 100) String metric,
        @Size(max = 360) String interpretation,
        @Size(max = 80) String severity
) {
}
