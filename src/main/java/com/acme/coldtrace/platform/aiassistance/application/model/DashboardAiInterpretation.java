package com.acme.coldtrace.platform.aiassistance.application.model;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Advisory AI interpretation generated from organization dashboard evidence.
 *
 * @param organizationId organization that owns the source data
 * @param question optional operator question used for the generated answer
 * @param interpretation structured advisory interpretation
 * @param sourceMetrics factual dashboard metrics used as prompt references
 * @param modelProvider configured AI provider
 * @param modelName configured AI model
 * @param generatedAt generation timestamp
 * @since 1.0
 */
public record DashboardAiInterpretation(
        Long organizationId,
        String question,
        DashboardInterpretationDraft interpretation,
        List<DashboardSourceMetric> sourceMetrics,
        String modelProvider,
        String modelName,
        OffsetDateTime generatedAt
) {
    public DashboardAiInterpretation {
        sourceMetrics = sourceMetrics == null ? List.of() : List.copyOf(sourceMetrics);
    }
}
