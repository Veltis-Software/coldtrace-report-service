package com.acme.coldtrace.reports.interfaces.rest;
import com.acme.coldtrace.reports.infrastructure.ReportEvidenceStore;
import com.acme.coldtrace.platform.reports.application.ports.ReportEvidenceSnapshot;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
/** Publishes completed owner evidence without sharing persistence with consumers. */
@RestController
@RequestMapping("/internal/reports/organizations/{organizationId}/evidence")
public class InternalReportEvidenceController {
  private final ReportEvidenceStore reports;
  private final String key;
  public InternalReportEvidenceController(ReportEvidenceStore reports,
      @Value("${coldtrace.internal.service-key:}")String key) { this.reports=reports; this.key=key; }
  private void authorize(String candidate,long org) {
    if(key.length()<32)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
    if(candidate==null||!MessageDigest.isEqual(key.getBytes(StandardCharsets.UTF_8),candidate.getBytes(StandardCharsets.UTF_8)))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    if(org<1)throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  }
  @GetMapping
  public List<ReportEvidenceSnapshot> recent(@PathVariable long organizationId,
      @RequestHeader(value="X-Service-Key",required=false)String candidate) {
    authorize(candidate,organizationId); return reports.findAllByOrganizationId(organizationId);
  }
  @GetMapping("/{reportId}")
  public ReportEvidenceSnapshot detail(@PathVariable long organizationId,@PathVariable long reportId,
      @RequestHeader(value="X-Service-Key",required=false)String candidate) {
    authorize(candidate,organizationId);
    return reports.findByIdAndOrganizationId(reportId,organizationId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  }
}
