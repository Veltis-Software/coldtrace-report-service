package com.acme.coldtrace.platform.reports.application.ports;

import java.time.OffsetDateTime;

/** Immutable report evidence published by its owner; no report entity is imported. */
public record ReportEvidenceSnapshot(
    Long id,
    Long organizationId,
    String uuid,
    String type,
    String title,
    OffsetDateTime periodStart,
    OffsetDateTime periodEnd,
    OffsetDateTime generatedAt,
    Integer assetCount,
    Integer readingCount,
    Integer outOfRangeReadingCount,
    Integer incidentCount,
    Integer openIncidentCount,
    Double averageTemperature,
    Double averageHumidity,
    Double compliancePercentage
) {}
