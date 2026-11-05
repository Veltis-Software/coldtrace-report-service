package com.acme.coldtrace.platform.billing.interfaces.acl;

/**
 * Marker for application failures caused by subscription plan entitlements.
 *
 * @since 1.0
 */
public interface PlanEntitlementFailure {
    /**
     * Returns the entitlement decision that blocked the operation.
     *
     * @return entitlement check metadata
     */
    SubscriptionBillingContextFacade.EntitlementCheckSnapshot entitlement();
}
