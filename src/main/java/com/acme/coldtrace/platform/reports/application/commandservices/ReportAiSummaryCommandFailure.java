package com.acme.coldtrace.platform.reports.application.commandservices;

import com.acme.coldtrace.platform.aiassistance.application.commandservices.AiAssistanceFailure;
import com.acme.coldtrace.platform.billing.interfaces.acl.PlanEntitlementFailure;
import com.acme.coldtrace.platform.billing.interfaces.acl.SubscriptionBillingContextFacade;

/**
 * Failure types for AI report summary generation.
 *
 * @since 1.0
 */
public sealed interface ReportAiSummaryCommandFailure
        permits ReportAiSummaryCommandFailure.OrganizationNotFound,
        ReportAiSummaryCommandFailure.ReportNotFound,
        ReportAiSummaryCommandFailure.ContextAssemblyFailed,
        ReportAiSummaryCommandFailure.ProviderFailure,
        ReportAiSummaryCommandFailure.PlanLimitExceeded {
    /** @return message key to resolve through i18n */
    String messageKey();

    /** @return optional message interpolation arguments */
    default Object[] args() {
        return new Object[0];
    }

    /** Organization not found failure. */
    record OrganizationNotFound() implements ReportAiSummaryCommandFailure {
        @Override
        public String messageKey() {
            return "reports.report.error.organization-not-found";
        }
    }

    /** ReportEvidenceSnapshot not found failure. */
    record ReportNotFound() implements ReportAiSummaryCommandFailure {
        @Override
        public String messageKey() {
            return "reports.report.error.report-not-found";
        }
    }

    /** Backend report context could not be serialized for the AI prompt. */
    record ContextAssemblyFailed() implements ReportAiSummaryCommandFailure {
        @Override
        public String messageKey() {
            return "reports.ai-summary.error.context-unavailable";
        }
    }

    /** AI provider failure while generating structured summary content. */
    record ProviderFailure(AiAssistanceFailure failure) implements ReportAiSummaryCommandFailure {
        @Override
        public String messageKey() {
            return failure.messageKey();
        }

        @Override
        public Object[] args() {
            return failure.args();
        }
    }

    /** Subscription plan does not allow AI report summaries. */
    record PlanLimitExceeded(SubscriptionBillingContextFacade.EntitlementCheckSnapshot entitlement)
            implements ReportAiSummaryCommandFailure, PlanEntitlementFailure {
        @Override
        public String messageKey() {
            return "reports.ai-summary.error.plan-limit-exceeded";
        }
    }
}
