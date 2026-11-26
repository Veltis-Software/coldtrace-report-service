package com.acme.coldtrace.platform.reports.interfaces.rest.resources;

import java.time.OffsetDateTime;

/**
 * REST resource representing a generated report.
 *
 * @param id persistence identifier
 * @param organizationId organization that owns the report
 * @param uuid report business identifier
 * @param type report type
 * @param title human-readable title
 * @param periodDate compatibility period label for frontend clients
 * @param periodStart inclusive lower data bound
 * @param periodEnd inclusive upper data bound
 * @param generatedAt generation timestamp
 * @param assetCount number of assets considered
 * @param readingCount number of readings considered
 * @param outOfRangeReadingCount number of out-of-range readings
 * @param incidentCount number of incidents considered
 * @param openIncidentCount number of unresolved incidents
 * @param averageTemperature average temperature across readings
 * @param averageHumidity average humidity across readings
 * @param compliancePercentage percentage of in-range readings
 * @since 1.0
 */
public record ReportResource(
        Long id,
        Long organizationId,
        String uuid,
        String type,
        String title,
        String periodDate,
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
) {
}
