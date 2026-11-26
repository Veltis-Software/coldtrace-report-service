package com.acme.coldtrace.platform.reports.interfaces.rest;

import com.acme.coldtrace.reports.infrastructure.IdentityGateway;
import com.acme.coldtrace.platform.reports.application.commandservices.ReportAiSummaryCommandService;
import com.acme.coldtrace.platform.reports.domain.model.commands.GenerateReportAiSummaryCommand;
import com.acme.coldtrace.platform.reports.interfaces.rest.transform.ResponseEntityFromReportAiSummaryCommandResultAssembler;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/reports/{reportId}/ai-summary")
public class ReportAiSummaryController {
  private final IdentityGateway identity;
  private final ReportAiSummaryCommandService summaries;
  private final MessageSource messages;
  public ReportAiSummaryController(IdentityGateway identity, ReportAiSummaryCommandService summaries, MessageSource messages) {
    this.identity=identity; this.summaries=summaries; this.messages=messages;
  }
  @PostMapping
  public ResponseEntity<?> generate(@PathVariable Long organizationId,@PathVariable Long reportId,
      @RequestHeader(value="Authorization",required=false)String bearer) {
    var session=identity.session(bearer);
    if(!session.organizationId().equals(organizationId)||!session.permits("reports:view"))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    return ResponseEntityFromReportAiSummaryCommandResultAssembler.toResponseEntityFromGenerationResult(
        summaries.handle(new GenerateReportAiSummaryCommand(organizationId,reportId)), messages);
  }
}
