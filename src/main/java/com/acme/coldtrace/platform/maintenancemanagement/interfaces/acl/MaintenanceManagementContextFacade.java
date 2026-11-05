package com.acme.coldtrace.platform.maintenancemanagement.interfaces.acl;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Published anti-corruption facade for the maintenance management context.
 * <p>
 * Other bounded contexts consume immutable maintenance snapshots instead of
 * importing maintenance repositories or aggregates directly.
 *
 * @since 1.0
 */
public interface MaintenanceManagementContextFacade {
    /**
     * Fetches maintenance schedules for one organization asset.
     *
     * @param organizationId organization identifier
     * @param assetId asset identifier
     * @return schedules associated with the asset
     */
    List<MaintenanceScheduleSnapshot> fetchMaintenanceSchedulesByOrganizationIdAndAssetId(
            Long organizationId,
            Long assetId
    );

    /**
     * Fetches technical service requests tied to one incident.
     *
     * @param organizationId organization identifier
     * @param incidentId incident identifier
     * @return technical service requests associated with the incident
     */
    List<TechnicalServiceRequestSnapshot> fetchTechnicalServiceRequestsByOrganizationIdAndIncidentId(
            Long organizationId,
            Long incidentId
    );

    /**
     * Fetches technical service requests tied to one organization asset.
     *
     * @param organizationId organization identifier
     * @param assetId asset identifier
     * @return technical service requests associated with the asset
     */
    List<TechnicalServiceRequestSnapshot> fetchTechnicalServiceRequestsByOrganizationIdAndAssetId(
            Long organizationId,
            Long assetId
    );

    /**
     * Preventive maintenance schedule data published to other contexts.
     */
    record MaintenanceScheduleSnapshot(
            Long id,
            Long organizationId,
            String uuid,
            Long assetId,
            OffsetDateTime scheduledDate,
            Integer frequencyDays,
            Long responsibleUserId,
            String observations,
            String status,
            OffsetDateTime registeredAt
    ) {
    }

    /**
     * Corrective technical service request data published to other contexts.
     */
    record TechnicalServiceRequestSnapshot(
            Long id,
            Long organizationId,
            String code,
            Long assetId,
            Long assetLocationId,
            String assetName,
            Long incidentId,
            String issueDescription,
            String priority,
            String status,
            String requestedBy,
            OffsetDateTime requestedAt,
            OffsetDateTime closedAt,
            String closureSummary,
            String evidence,
            String closedBy
    ) {
    }
}
