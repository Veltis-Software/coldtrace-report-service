package com.acme.coldtrace.platform.aiassistance.application.model;

/**
 * Factual dashboard metric included as a source reference for AI interpretation.
 *
 * @param name metric name
 * @param value metric value formatted for rendering and prompting
 * @param unit optional unit
 * @param description short metric definition
 * @since 1.0
 */
public record DashboardSourceMetric(
        String name,
        String value,
        String unit,
        String description
) {
}
