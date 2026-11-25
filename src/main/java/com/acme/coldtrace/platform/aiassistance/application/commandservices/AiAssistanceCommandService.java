package com.acme.coldtrace.platform.aiassistance.application.commandservices;

import com.acme.coldtrace.platform.aiassistance.application.model.AiGeneratedResponse;
import com.acme.coldtrace.platform.aiassistance.application.model.ComplianceSummaryDraft;
import com.acme.coldtrace.platform.aiassistance.application.model.DashboardInterpretationDraft;
import com.acme.coldtrace.platform.aiassistance.application.model.IncidentResolutionPlanDraft;
import com.acme.coldtrace.platform.aiassistance.domain.model.commands.GenerateComplianceSummaryCommand;
import com.acme.coldtrace.platform.aiassistance.domain.model.commands.GenerateDashboardInterpretationCommand;
import com.acme.coldtrace.platform.aiassistance.domain.model.commands.GenerateIncidentResolutionPlanDraftCommand;
import com.acme.coldtrace.platform.shared.application.result.Result;

/**
 * Application service contract for AI-assisted advisory content.
 *
 * @since 1.0
 */
public interface AiAssistanceCommandService {
    /**
     * Generates a structured advisory incident resolution plan.
     *
     * @param command command containing backend-assembled incident context
     * @return generated plan or controlled failure
     */
    Result<AiGeneratedResponse<IncidentResolutionPlanDraft>, AiAssistanceFailure> handle(
            GenerateIncidentResolutionPlanDraftCommand command);

    /**
     * Generates a structured dashboard interpretation.
     *
     * @param command command containing backend-assembled dashboard context
     * @return generated interpretation or controlled failure
     */
    Result<AiGeneratedResponse<DashboardInterpretationDraft>, AiAssistanceFailure> handle(
            GenerateDashboardInterpretationCommand command);

    /**
     * Generates a structured compliance summary.
     *
     * @param command command containing backend-assembled report context
     * @return generated summary or controlled failure
     */
    Result<AiGeneratedResponse<ComplianceSummaryDraft>, AiAssistanceFailure> handle(
            GenerateComplianceSummaryCommand command);
}
