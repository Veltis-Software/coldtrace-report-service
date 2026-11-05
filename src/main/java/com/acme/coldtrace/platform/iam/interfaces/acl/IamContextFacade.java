package com.acme.coldtrace.platform.iam.interfaces.acl;

import java.util.List;

/**
 * Published anti-corruption facade for the IAM bounded context.
 * <p>
 * Other bounded contexts use this interface instead of depending on identity
 * repositories or aggregates directly. The facade exposes only the small
 * organization-related language needed by current Sprint 3 use cases.
 *
 * @since 1.0
 */
public interface IamContextFacade {
    /**
     * Checks whether an organization exists.
     *
     * @param organizationId organization identifier
     * @return {@code true} when the organization exists
     */
    boolean organizationExists(Long organizationId);

    /**
     * Checks whether a user exists inside an organization.
     *
     * @param organizationId organization identifier
     * @param userId user identifier
     * @return {@code true} when the user belongs to the organization
     */
    boolean userExistsByIdAndOrganizationId(Long organizationId, Long userId);

    /**
     * Counts users that belong to an organization.
     *
     * @param organizationId organization identifier
     * @return number of users in the organization
     */
    int countUsersByOrganizationId(Long organizationId);

    /**
     * Fetches organization identifiers known by IAM.
     *
     * @return organization identifiers
     */
    List<Long> fetchOrganizationIds();
}
