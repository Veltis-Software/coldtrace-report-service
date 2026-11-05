package com.acme.coldtrace.platform.billing.interfaces.acl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exposes plan entitlement metadata as neutral API response properties.
 *
 * @since 1.0
 */
public final class PlanEntitlementProblemProperties {
    private PlanEntitlementProblemProperties() {
    }

    /**
     * Converts an entitlement decision into properties that REST assemblers can attach to a problem response.
     *
     * @param entitlement entitlement decision
     * @return response properties with null values omitted
     */
    public static Map<String, Object> from(
            SubscriptionBillingContextFacade.EntitlementCheckSnapshot entitlement
    ) {
        var properties = new LinkedHashMap<String, Object>();
        putIfPresent(properties, "organizationId", entitlement.organizationId());
        putIfPresent(properties, "planCode", entitlement.planCode());
        putIfPresent(properties, "subscriptionStatus", entitlement.subscriptionStatus());
        putIfPresent(properties, "entitlementKey", entitlement.key());
        putIfPresent(properties, "entitlementCategory", entitlement.category());
        putIfPresent(properties, "entitlementEnabled", entitlement.enabled());
        putIfPresent(properties, "limit", entitlement.limit());
        putIfPresent(properties, "used", entitlement.used());
        putIfPresent(properties, "remaining", entitlement.remaining());
        putIfPresent(properties, "lockedReason", entitlement.lockedReason());
        putIfPresent(properties, "requiredPlanCode", entitlement.requiredPlanCode());
        return properties;
    }

    private static void putIfPresent(Map<String, Object> properties, String key, Object value) {
        if (value != null) {
            properties.put(key, value);
        }
    }
}
