package com.acme.coldtrace.reports.infrastructure;
import com.acme.coldtrace.platform.reports.application.ports.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
@Component
public class ReportEvidenceStore implements ReportEvidenceReader {
  private final JdbcTemplate jdbc;
  private final ObjectMapper json;
  public ReportEvidenceStore(JdbcTemplate jdbc, ObjectMapper json) { this.jdbc=jdbc; this.json=json; }
  private static OffsetDateTime time(ResultSet row, String field) throws SQLException {
    var value = row.getTimestamp(field, Calendar.getInstance(TimeZone.getTimeZone("UTC")));
    return value == null ? null : value.toInstant().atOffset(ZoneOffset.UTC);
  }
  private ReportEvidenceSnapshot snapshot(ResultSet row, int index) throws SQLException {
    JsonNode result;
    try { result = json.readTree(row.getString("result")); }
    catch (Exception invalid) { throw new SQLException("Invalid persisted report result", invalid); }
    var readings = result.path("readings");
    var incidents = result.path("incidents");
    int breached = 0, open = 0, tCount = 0, hCount = 0;
    double temperature = 0, humidity = 0;
    boolean knownRanges = readings.isArray();
    for (var reading : readings) {
      if (!reading.path("outOfRange").isBoolean()) knownRanges = false;
      else if (reading.path("outOfRange").asBoolean()) breached++;
      if (reading.path("temperature").isNumber()) { temperature += reading.get("temperature").asDouble(); tCount++; }
      if (reading.path("humidity").isNumber()) { humidity += reading.get("humidity").asDouble(); hCount++; }
    }
    boolean knownStatuses = incidents.isArray();
    for (var incident : incidents) {
      var status = incident.path("incident").path("status");
      if (!status.isTextual()) knownStatuses = false;
      else if (!List.of("RESOLVED", "CLOSED").contains(status.asText().toUpperCase(java.util.Locale.ROOT))) open++;
    }
    int count = readings.size();
    return new ReportEvidenceSnapshot(row.getLong("id"), row.getLong("organization_id"), null, "traceability", row.getString("title"),
        time(row, "period_start"), time(row, "period_end"), time(row, "completed_at"),
        result.path("assets").isArray() ? result.get("assets").size() : null,
        readings.isArray() ? count : null, knownRanges ? breached : null,
        incidents.isArray() ? incidents.size() : null, knownStatuses ? open : null,
        tCount == 0 ? null : temperature / tCount, hCount == 0 ? null : humidity / hCount,
        knownRanges && count > 0 ? 100.0 * (count - breached) / count : null);
  }

  public List<ReportEvidenceSnapshot> findAllByOrganizationId(Long organizationId) {
    return jdbc.query("SELECT * FROM reports WHERE organization_id=? AND status='READY' ORDER BY completed_at DESC,id DESC LIMIT 100", this::snapshot, organizationId);
  }
  public Optional<ReportEvidenceSnapshot> findByIdAndOrganizationId(Long id, Long organizationId) {
    return jdbc.query("SELECT * FROM reports WHERE organization_id=? AND id=? AND status='READY'", this::snapshot, organizationId,id).stream().findFirst();
  }
}
