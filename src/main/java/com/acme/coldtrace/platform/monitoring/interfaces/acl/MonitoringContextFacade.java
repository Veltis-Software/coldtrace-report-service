package com.acme.coldtrace.platform.monitoring.interfaces.acl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Published anti-corruption facade for the monitoring bounded context.
 * <p>
 * Other bounded contexts consume immutable reading snapshots instead of
 * importing monitoring repositories or aggregates directly.
 *
 * @since 1.0
 */
public interface MonitoringContextFacade {
    /**
     * Fetches sensor reading snapshots for an organization.
     *
     * @param organizationId organization identifier
     * @return readings owned by the organization
     */
    List<SensorReadingSnapshot> fetchSensorReadingsByOrganizationId(Long organizationId);

    /**
     * Fetches one sensor reading snapshot by organization and reading identifiers.
     *
     * @param organizationId organization identifier
     * @param sensorReadingId sensor reading identifier
     * @return reading snapshot when it belongs to the organization
     */
    Optional<SensorReadingSnapshot> fetchSensorReadingByIdAndOrganizationId(Long organizationId, Long sensorReadingId);

    /**
     * Sensor reading data published to other bounded contexts.
     */
    record SensorReadingSnapshot(
            Long id,
            Long organizationId,
            Long assetId,
            Long iotDeviceId,
            Long gatewayId,
            Long locationId,
            Double temperature,
            Double humidity,
            Boolean outOfRange,
            OffsetDateTime recordedAt
    ) {
    }
}
