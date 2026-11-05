package com.acme.coldtrace.platform.billing.interfaces.acl;

import java.util.List;
import java.util.Optional;

/**
 * Published anti-corruption facade for subscription and entitlement checks.
 *
 * @since 1.0
 */
public interface SubscriptionBillingContextFacade {
    String ENTITLEMENT_LOCATIONS = "locations";
    String ENTITLEMENT_ASSETS = "assets";
    String ENTITLEMENT_IOT_DEVICES = "iot-devices";
    String ENTITLEMENT_USERS = "users";
    String ENTITLEMENT_REPORT_HISTORY = "report-history";
    String ENTITLEMENT_MAINTENANCE = "maintenance";
    String ENTITLEMENT_AI_GUIDANCE = "ai-guidance";
    String ENTITLEMENT_AI_REPORT_SUMMARY = "ai-report-summary";

    /**
     * Ensures a newly created organization has its initial Base subscription.
     *
     * @param organizationId organization identifier
     */
    void initializeBaseSubscriptionForOrganization(Long organizationId);

    /**
     * Fetches the current entitlement snapshot for an organization.
     *
     * @param organizationId organization identifier
     * @return entitlement snapshot when the organization and subscription plan exist
     */
    Optional<OrganizationEntitlementsSnapshot> fetchEntitlementsByOrganizationId(Long organizationId);

    /**
     * Checks a specific entitlement and returns metadata for blocked operations.
     *
     * @param organizationId organization identifier
     * @param entitlementKey stable entitlement key
     * @return entitlement check metadata when the key can be evaluated
     */
    Optional<EntitlementCheckSnapshot> checkEntitlement(Long organizationId, String entitlementKey);

    /**
     * Checks whether an organization can use a specific entitlement now.
     *
     * @param organizationId organization identifier
     * @param entitlementKey stable entitlement key
     * @return true when the entitlement is enabled
     */
    boolean canUseEntitlement(Long organizationId, String entitlementKey);

    /**
     * Subscription entitlement data published to other contexts.
     */
    record OrganizationEntitlementsSnapshot(
            Long organizationId,
            String planCode,
            String status,
            List<EntitlementItemSnapshot> entitlements
    ) {
    }

    /**
     * One entitlement item published to other contexts.
     */
    record EntitlementItemSnapshot(
            String key,
            String category,
            Boolean enabled,
            Integer limit,
            Integer used,
            Integer remaining,
            String lockedReason
    ) {
    }

    /**
     * One entitlement decision published to other contexts for enforcement.
     */
    record EntitlementCheckSnapshot(
            Long organizationId,
            String planCode,
            String subscriptionStatus,
            String key,
            String category,
            Boolean enabled,
            Integer limit,
            Integer used,
            Integer remaining,
            String lockedReason,
            String requiredPlanCode
    ) {
    }
}
