package com.acme.coldtrace.platform.reports.interfaces.rest;

import com.acme.coldtrace.reports.infrastructure.IdentityGateway;
import com.acme.coldtrace.platform.reports.application.ports.ReportEvidenceReader;
import com.acme.coldtrace.platform.reports.interfaces.rest.resources.ReportResource;
import com.acme.coldtrace.platform.reports.interfaces.rest.transform.ReportResourceFromEntityAssembler;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Organization read API for completed reports and their preserved source metrics. */
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/reports")
public class OrganizationReportQueriesController {
  private final IdentityGateway identity;
  private final ReportEvidenceReader reports;
  public OrganizationReportQueriesController(IdentityGateway identity,ReportEvidenceReader reports) {
    this.identity=identity;this.reports=reports;
  }
  private void authorize(String bearer,Long org) {
    var session=identity.session(bearer);
    if(!session.organizationId().equals(org)||!session.permits("reports:view"))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  }
  @GetMapping
  public List<ReportResource> list(@PathVariable Long organizationId,
      @RequestHeader(value="Authorization",required=false)String bearer) {
    authorize(bearer,organizationId);
    return reports.findAllByOrganizationId(organizationId).stream()
        .map(ReportResourceFromEntityAssembler::toResourceFromEntity).toList();
  }
  @GetMapping("/{reportId}")
  public ReportResource detail(@PathVariable Long organizationId,@PathVariable Long reportId,
      @RequestHeader(value="Authorization",required=false)String bearer) {
    authorize(bearer,organizationId);
    return ReportResourceFromEntityAssembler.toResourceFromEntity(reports.findByIdAndOrganizationId(reportId,organizationId)
        .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND)));
  }
}
