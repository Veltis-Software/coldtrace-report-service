package com.acme.coldtrace.reports.application;

import com.acme.coldtrace.shared.IdentitySession;
import com.fasterxml.jackson.databind.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReportJobs {
  private final JdbcTemplate jdbc;
  private final ObjectMapper json;

  public ReportJobs(JdbcTemplate jdbc, ObjectMapper json) {
    this.jdbc = jdbc;
    this.json = json;
  }

  public record Request(String title, Instant periodStart, Instant periodEnd) {}

  public record Job(long id, String status, String resultUrl) {}

  public static void validate(Request request) {
    if (request.title() == null
        || request.title().isBlank()
        || request.title().length() > 200
        || request.periodStart() == null
        || request.periodEnd() == null
        || !request.periodStart().isBefore(request.periodEnd())
        || Duration.between(request.periodStart(), request.periodEnd()).toDays() > 90)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Invalid report request; period is [start,end), at most 90 days");
  }

  private String serialize(Object value) {
    try {
      return json.writeValueAsString(value);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private JsonNode parse(Object value) {
    try {
      var node =
          json.readTree(
              value instanceof byte[] bytes
                  ? new String(bytes, java.nio.charset.StandardCharsets.UTF_8)
                  : value.toString());
      return node.isTextual() ? json.readTree(node.asText()) : node;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Transactional
  public Job request(IdentitySession session, Request request, JsonNode incidents) {
    validate(request);
    if (!session.permits("reports:view")) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    var holder = new GeneratedKeyHolder();
    jdbc.update(
        connection -> {
          var statement =
              connection.prepareStatement(
                  "INSERT INTO"
                      + " reports(organization_id,requested_by,title,period_start,period_end,status,incident_snapshot,created_at)"
                      + " VALUES (?,?,?,?,?,'PENDING',?,?)",
                  Statement.RETURN_GENERATED_KEYS);
          Object[] values = {
            session.organizationId(),
            session.userId(),
            request.title().trim(),
            Timestamp.from(request.periodStart()),
            Timestamp.from(request.periodEnd()),
            serialize(incidents),
            Timestamp.from(Instant.now())
          };
          for (int i = 0; i < values.length; i++) statement.setObject(i + 1, values[i]);
          return statement;
        },
        holder);
    long id = Objects.requireNonNull(holder.getKey()).longValue();
    return new Job(id, "PENDING", "/api/v1/reports/" + id + "/result");
  }

  public Map<String, Object> status(long id, long org) {
    var rows =
        jdbc.queryForList(
            "SELECT id,title,status,period_start,period_end,created_at,completed_at FROM reports"
                + " WHERE id=? AND organization_id=?",
            id,
            org);
    if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    return rows.get(0);
  }

  public JsonNode result(long id, long org) {
    var rows =
        jdbc.queryForList(
            "SELECT status,result FROM reports WHERE id=? AND organization_id=?", id, org);
    if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    if (!"READY".equals(rows.get(0).get("status")))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "REPORT_NOT_READY");
    return parse(rows.get(0).get("result"));
  }

  @Scheduled(fixedDelayString = "${coldtrace.reports.interval-ms:1000}")
  @Transactional
  public void generatePending() {
    var jobs =
        jdbc.queryForList(
            "SELECT * FROM reports WHERE status='PENDING' ORDER BY id LIMIT 1 FOR UPDATE SKIP"
                + " LOCKED");
    for (var job : jobs) {
      long id = ((Number) job.get("id")).longValue(),
          org = ((Number) job.get("organization_id")).longValue();
      var readings =
          jdbc.queryForList(
              "SELECT payload FROM reading_samples WHERE organization_id=? AND recorded_at>=? AND"
                  + " recorded_at<? ORDER BY recorded_at,reading_id",
              org,
              job.get("period_start"),
              job.get("period_end"));
      var samples = json.createArrayNode();
      for (var row : readings) samples.add(parse(row.get("payload")));
      var assets = new LinkedHashMap<Long, String>();
      for (var sample : samples)
        assets.put(
            sample.path("assetId").asLong(),
            sample.path("assetName").asText("Asset " + sample.path("assetId").asLong()));
      var incidents = parse(job.get("incident_snapshot"));
      for (var incident : incidents)
        assets.put(
            incident.path("asset").path("assetId").asLong(),
            incident.path("asset").path("name").asText());
      var result = new LinkedHashMap<String, Object>();
      result.put("title", job.get("title"));
      result.put("periodStart", job.get("period_start").toString());
      result.put("periodEnd", job.get("period_end").toString());
      result.put("generatedAt", Instant.now());
      result.put(
          "assets",
          assets.entrySet().stream()
              .map(entry -> Map.of("assetId", entry.getKey(), "name", entry.getValue()))
              .toList());
      result.put("readings", samples);
      result.put("incidents", incidents);
      result.put("readingCount", samples.size());
      result.put("incidentCount", incidents.size());
      jdbc.update(
          "UPDATE reports SET result=?,status='READY',completed_at=? WHERE id=?",
          serialize(result),
          Timestamp.from(Instant.now()),
          id);
    }
  }
}
