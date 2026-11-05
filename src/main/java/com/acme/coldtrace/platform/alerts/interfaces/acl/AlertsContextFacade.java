package com.acme.coldtrace.platform.alerts.interfaces.acl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Published anti-corruption facade for the alerts bounded context.
 * <p>
 * Reporting consumes incident snapshots through this facade instead of reading
 * alerts repositories or aggregates directly.
 *
 * @since 1.0
 */
public interface AlertsContextFacade {
    /**
     * Fetches incident snapshots for an organization.
     *
     * @param organizationId organization identifier
     * @return incidents owned by the organization
     */
    List<IncidentSnapshot> fetchIncidentsByOrganizationId(Long organizationId);

    /**
     * Fetches one incident snapshot by organization and incident identifiers.
     *
     * @param organizationId organization identifier
     * @param incidentId incident identifier
     * @return incident snapshot when it belongs to the organization
     */
    Optional<IncidentSnapshot> fetchIncidentByIdAndOrganizationId(Long organizationId, Long incidentId);

    /**
     * Incident data published to other bounded contexts.
     */
    record IncidentSnapshot(Long id, Long organizationId, Long assetId, String status, Instant detectedAt) {
        /**
         * Checks whether the incident is still open from an operational view.
         *
         * @return {@code true} when the incident has not been resolved
         */
        public boolean isOpen() {
            return !"resolved".equalsIgnoreCase(status);
        }
    }
}
