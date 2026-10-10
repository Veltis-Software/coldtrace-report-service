package com.acme.coldtrace.reports.interfaces.rest;

import com.acme.coldtrace.reports.application.*;
import com.acme.coldtrace.reports.infrastructure.IdentityGateway;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ReportsController {
  private final IdentityGateway identity;private final ReportJobs jobs;private final RestClient alerts;
  public ReportsController(IdentityGateway identity,ReportJobs jobs,@Value("${coldtrace.alert.url}")String url) {
    this.identity=identity;this.jobs=jobs;var factory=new org.springframework.http.client.JdkClientHttpRequestFactory(java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build());factory.setReadTimeout(Duration.ofSeconds(10));alerts=RestClient.builder().baseUrl(url).requestFactory(factory).build();
  }
  @PostMapping("/api/v1/reports") @ResponseStatus(HttpStatus.ACCEPTED)
  public ReportJobs.Job create(@RequestBody ReportJobs.Request request,@RequestHeader(value="Authorization",required=false)String bearer) {
    var session=identity.session(bearer);if(!session.permits("reports:view"))throw new ResponseStatusException(HttpStatus.FORBIDDEN);ReportJobs.validate(request);
    // Snapshot owned Alert data through its API. No cross-schema reads or stored bearer token.
    var incidents=alerts.get().uri(builder -> builder.path("/api/v1/incidents/traceability").queryParam("from",request.periodStart()).queryParam("to",request.periodEnd()).build()).header("Authorization",bearer).retrieve().body(JsonNode.class);
    if(incidents==null||!incidents.isArray())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Incident snapshot unavailable");
    return jobs.request(session,request,incidents);
  }
  @GetMapping("/api/v1/reports/{id}")
  public Map<String,Object> status(@PathVariable long id,@RequestHeader(value="Authorization",required=false)String bearer) {var session=identity.session(bearer); if(!session.permits("reports:view"))throw new ResponseStatusException(HttpStatus.FORBIDDEN); return jobs.status(id,session.organizationId());}
  @GetMapping("/api/v1/reports/{id}/result")
  public JsonNode result(@PathVariable long id,@RequestHeader(value="Authorization",required=false)String bearer) {var session=identity.session(bearer); if(!session.permits("reports:view"))throw new ResponseStatusException(HttpStatus.FORBIDDEN); return jobs.result(id,session.organizationId());}
}
