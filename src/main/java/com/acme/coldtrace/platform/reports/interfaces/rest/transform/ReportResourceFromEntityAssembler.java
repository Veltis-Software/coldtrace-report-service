package com.acme.coldtrace.platform.reports.interfaces.rest.transform;

import com.acme.coldtrace.platform.reports.application.ports.ReportEvidenceSnapshot;
import com.acme.coldtrace.platform.reports.interfaces.rest.resources.ReportResource;

import java.time.format.DateTimeFormatter;

/**
 * Assembler that converts report aggregates into REST resources.
 *
 * @since 1.0
 */
public class ReportResourceFromEntityAssembler {
    /**
     * Converts a report aggregate into a resource.
     *
     * @param entity domain aggregate
     * @return REST resource with report data and metrics
     */
    public static ReportResource toResourceFromEntity(ReportEvidenceSnapshot entity) {
        return new ReportResource(
                entity.id(),
                entity.organizationId(),
                entity.uuid(),
                entity.type(),
                entity.title(),
                periodLabel(entity),
                entity.periodStart(),
                entity.periodEnd(),
                entity.generatedAt(),
                entity.assetCount(),
                entity.readingCount(),
                entity.outOfRangeReadingCount(),
                entity.incidentCount(),
                entity.openIncidentCount(),
                entity.averageTemperature(),
                entity.averageHumidity(),
                entity.compliancePercentage()
        );
    }

    private static String periodLabel(ReportEvidenceSnapshot entity) {
        var formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        return entity.periodStart().format(formatter) + "/" + entity.periodEnd().format(formatter);
    }
}
