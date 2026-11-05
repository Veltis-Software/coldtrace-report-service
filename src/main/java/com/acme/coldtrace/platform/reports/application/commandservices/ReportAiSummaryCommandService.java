package com.acme.coldtrace.platform.reports.application.commandservices;

import com.acme.coldtrace.platform.reports.application.model.ReportAiSummary;
import com.acme.coldtrace.platform.reports.domain.model.commands.GenerateReportAiSummaryCommand;
import com.acme.coldtrace.platform.shared.application.result.Result;

/**
 * Application service contract for AI-assisted report summaries.
 *
 * @since 1.0
 */
public interface ReportAiSummaryCommandService {
    /**
     * Generates a structured advisory summary for a persisted report.
     *
     * @param command command containing report scope
     * @return generated advisory summary or controlled failure
     */
    Result<ReportAiSummary, ReportAiSummaryCommandFailure> handle(GenerateReportAiSummaryCommand command);
}
