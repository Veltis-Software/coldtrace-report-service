package com.acme.coldtrace.reports.application;

import com.fasterxml.jackson.databind.*;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReadingProjection {
  private final JdbcTemplate jdbc;
  private final ObjectMapper json;

  public ReadingProjection(JdbcTemplate jdbc, ObjectMapper json) {
    this.jdbc = jdbc;
    this.json = json;
  }

  @Transactional
  public void accept(JsonNode event) {
    UUID id = UUID.fromString(event.path("eventId").asText());
    long org = event.path("organizationId").asLong();
    String type = event.path("eventType").asText();
    if (org <= 0
        || event.path("version").asInt() != 1
        || !Set.of("readings.ingested", "incident.opened").contains(type))
      throw new IllegalArgumentException("Invalid report event");
    if (jdbc.update(
            "INSERT IGNORE INTO processed_events(consumer,event_id,processed_at) VALUES"
                + " ('report-service',?,?)",
            id.toString(),
            Timestamp.from(Instant.now()))
        == 0) return;
    var payload = event.path("payload");
    if (type.equals("incident.opened")) {
      long incident = payload.path("incidentId").asLong(), asset = payload.path("assetId").asLong();
      Instant at = Instant.parse(payload.path("detectedAt").asText(event.path("occurredAt").asText()));
      if (incident <= 0 || asset <= 0)
        throw new IllegalArgumentException("Invalid incident reference");
      jdbc.update(
          "INSERT IGNORE INTO"
              + " incident_projection(organization_id,incident_id,asset_id,detected_at,payload)"
              + " VALUES (?,?,?,?,?)",
          org,
          incident,
          asset,
          Timestamp.from(at),
          payload.toString());
      return;
    }
    if (!payload.path("readings").isArray() || payload.path("readings").size() > 500)
      throw new IllegalArgumentException("Invalid readings event");
    for (var reading : payload.path("readings")) {
      long readingId = reading.path("readingId").asLong(), asset = reading.path("assetId").asLong();
      Instant at = Instant.parse(reading.path("recordedAt").asText());
      if (readingId <= 0 || asset <= 0)
        throw new IllegalArgumentException("Invalid reading reference");
      Double temperature =
          reading.hasNonNull("temperature") ? reading.get("temperature").doubleValue() : null;
      Double humidity =
          reading.hasNonNull("humidity") ? reading.get("humidity").doubleValue() : null;
      if ((temperature != null && !Double.isFinite(temperature))
          || (humidity != null && !Double.isFinite(humidity)))
        throw new IllegalArgumentException("Invalid measurement");
      if (jdbc.update(
              "INSERT IGNORE INTO"
                  + " reading_samples(organization_id,reading_id,asset_id,recorded_at,payload)"
                  + " VALUES (?,?,?,?,?)",
              org,
              readingId,
              asset,
              Timestamp.from(at),
              reading.toString())
          == 0) continue;
      var hour = Timestamp.from(at.truncatedTo(ChronoUnit.HOURS));
      jdbc.update(
          "INSERT IGNORE INTO"
              + " reading_aggregates(organization_id,asset_id,hour_start,reading_count,out_of_range_count,sum_temperature,sum_humidity,temperature_count,humidity_count,updated_at)"
              + " VALUES (?,?,?,0,0,0,0,0,0,?)",
          org,
          asset,
          hour,
          Timestamp.from(Instant.now()));
      var rows =
          jdbc.queryForList(
              "SELECT * FROM reading_aggregates WHERE asset_id=? AND hour_start=? FOR UPDATE",
              asset,
              hour);
      if (((Number) rows.get(0).get("organization_id")).longValue() != org)
        throw new IllegalArgumentException("Asset organization mismatch");
      jdbc.update(
          "UPDATE reading_aggregates SET"
              + " reading_count=reading_count+1,out_of_range_count=out_of_range_count+?,temperature_count=temperature_count+?,humidity_count=humidity_count+?,sum_temperature=COALESCE(sum_temperature,0)+?,sum_humidity=COALESCE(sum_humidity,0)+?,min_temperature=CASE"
              + " WHEN ? IS NULL THEN min_temperature WHEN min_temperature IS NULL OR"
              + " min_temperature>? THEN ? ELSE min_temperature END,max_temperature=CASE WHEN ? IS"
              + " NULL THEN max_temperature WHEN max_temperature IS NULL OR max_temperature<? THEN"
              + " ? ELSE max_temperature END,min_humidity=CASE WHEN ? IS NULL THEN min_humidity"
              + " WHEN min_humidity IS NULL OR min_humidity>? THEN ? ELSE min_humidity"
              + " END,max_humidity=CASE WHEN ? IS NULL THEN max_humidity WHEN max_humidity IS NULL"
              + " OR max_humidity<? THEN ? ELSE max_humidity END,updated_at=? WHERE asset_id=? AND"
              + " hour_start=?",
          reading.path("outOfRange").asBoolean() ? 1 : 0,
          temperature == null ? 0 : 1,
          humidity == null ? 0 : 1,
          temperature == null ? 0 : temperature,
          humidity == null ? 0 : humidity,
          temperature,
          temperature,
          temperature,
          temperature,
          temperature,
          temperature,
          humidity,
          humidity,
          humidity,
          humidity,
          humidity,
          humidity,
          Timestamp.from(Instant.now()),
          asset,
          hour);
    }
  }
}
