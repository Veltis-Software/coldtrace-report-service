package com.acme.coldtrace.reports;

import com.acme.coldtrace.reports.interfaces.rest.InternalReportEvidenceController;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class InternalReportEvidenceTest {
  private static final String KEY = "isolated-report-owner-contract-key-at-least-32-characters";
  private JdbcTemplate jdbc;
  private InternalReportEvidenceController controller;
  @BeforeEach void fixture() {
    jdbc = new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", ""));
    jdbc.execute("CREATE TABLE reports(id BIGINT,organization_id BIGINT,title VARCHAR(200),status VARCHAR(20),result VARCHAR(4000),period_start TIMESTAMP,period_end TIMESTAMP,completed_at TIMESTAMP,report_type VARCHAR(32))");
    jdbc.update("INSERT INTO reports VALUES(1,7,'Conservacion Callao','READY',?,'2026-11-01 00:00:00','2026-11-02 00:00:00','2026-11-02 00:01:00','DAILY_LOG')", """
        {"assets":[{"assetId":20}],"readings":[{"temperature":4,"humidity":60,"outOfRange":false},{"temperature":10,"outOfRange":true}],"incidents":[{"id":1,"state":"OPEN"},{"id":2,"state":"RESOLVED"},{"incident":{"status":"RESOLVED"}}]}
        """);
    controller = new InternalReportEvidenceController(new com.acme.coldtrace.reports.infrastructure.ReportEvidenceStore(jdbc, new ObjectMapper()), KEY);
  }
  @Test void publishesMetricsFromTheImmutableCompletedReport() {
    var evidence = controller.detail(7, 1, KEY);
    assertEquals(2, evidence.readingCount());
    assertEquals(1, evidence.outOfRangeReadingCount());
    assertEquals(50.0, evidence.compliancePercentage());
    assertEquals(7.0, evidence.averageTemperature());
    assertEquals(60.0, evidence.averageHumidity());
    assertEquals(1, evidence.openIncidentCount());
    assertEquals("DAILY_LOG", evidence.type());
  }
  @Test void tenantScopeAndWorkloadCredentialAreRequired() {
    assertEquals(404, assertThrows(ResponseStatusException.class, () -> controller.detail(8, 1, KEY)).getStatusCode().value());
    assertEquals(401, assertThrows(ResponseStatusException.class, () -> controller.recent(7, "Bearer browser-token")).getStatusCode().value());
  }
  @Test void absentMeasurementsDoNotBecomeZeroOrFullCompliance() {
    jdbc.update("UPDATE reports SET result=? WHERE id=1", "{\"readings\":[{}],\"incidents\":[{}]}");
    var evidence = controller.detail(7, 1, KEY);
    assertNull(evidence.compliancePercentage());
    assertNull(evidence.averageTemperature());
    assertNull(evidence.openIncidentCount());
  }
}
