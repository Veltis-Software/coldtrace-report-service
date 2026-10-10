package com.acme.coldtrace.reports;

import static org.assertj.core.api.Assertions.*;
import com.acme.coldtrace.reports.application.*;
import com.acme.coldtrace.shared.IdentitySession;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:report;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1","spring.datasource.username=sa","spring.datasource.password=","spring.datasource.driver-class-name=org.h2.Driver","coldtrace.reports.interval-ms=3600000"})
class TraceabilityTest {
  @Autowired ReadingProjection projection;@Autowired ReportJobs jobs;@Autowired ObjectMapper json;@Autowired JdbcTemplate jdbc;
  @BeforeEach void clean(){for(String table:List.of("reports","reading_samples","reading_aggregates","incident_projection","processed_events"))jdbc.update("DELETE FROM "+table);}
  @Test void duplicateEventsAndReadingsDoNotInflateAggregates(){
    var reading=Map.of("readingId",1,"assetId",1,"recordedAt","2026-10-09T21:00:00Z","temperature",6,"outOfRange",false);
    var envelope=json.valueToTree(Map.of("eventId",UUID.randomUUID(),"eventType","readings.ingested","version",1,"organizationId",1,"payload",Map.of("readings",List.of(reading))));
    projection.accept(envelope);projection.accept(envelope);
    ((com.fasterxml.jackson.databind.node.ObjectNode)envelope).put("eventId",UUID.randomUUID().toString());projection.accept(envelope);
    assertThat(jdbc.queryForObject("SELECT reading_count FROM reading_aggregates",Integer.class)).isEqualTo(1);
    assertThat(jdbc.queryForObject("SELECT temperature_count FROM reading_aggregates",Integer.class)).isEqualTo(1);
    assertThat(jdbc.queryForObject("SELECT humidity_count FROM reading_aggregates",Integer.class)).isZero();
  }
  @Test void jobConsolidatesOwnedReadModelAndIncidentSnapshot(){
    var envelope=json.valueToTree(Map.of("eventId",UUID.randomUUID(),"eventType","readings.ingested","version",1,"organizationId",1,"payload",Map.of("readings",List.of(Map.of("readingId",1,"assetId",1,"assetName","Cold room","recordedAt","2026-10-09T21:00:00Z","temperature",6)))));projection.accept(envelope);
    var actor=new IdentitySession(10L,1L,"David",List.of("reports:view"));
    var incidents=json.valueToTree(List.of(Map.of("id",9,"asset",Map.of("assetId",1,"name","Cold room"),"assignee",Map.of("userId",10,"fullName","David"),"correctiveActions",List.of(Map.of("description","Cooling restored")))));
    var job=jobs.request(actor,new ReportJobs.Request("Traceability",Instant.parse("2026-10-09T20:00:00Z"),Instant.parse("2026-10-09T22:00:00Z")),incidents);jobs.generatePending();
    var result=jobs.result(job.id(),1);
    assertThat(result.path("readingCount").asInt()).isEqualTo(1);assertThat(result.path("incidentCount").asInt()).isEqualTo(1);
    assertThat(result.path("incidents").get(0).path("assignee").path("fullName").asText()).isEqualTo("David");
    assertThatThrownBy(() -> jobs.result(job.id(),2)).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
  }
  @Test void invalidEventRollsBackProcessedMarker(){
    var event=json.valueToTree(Map.of("eventId",UUID.randomUUID(),"eventType","readings.ingested","version",1,"organizationId",1,"payload",Map.of("readings",List.of(Map.of("readingId",0,"assetId",1,"recordedAt","2026-10-09T21:00:00Z")))));
    assertThatThrownBy(() -> projection.accept(event)).isInstanceOf(IllegalArgumentException.class);
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM processed_events",Long.class)).isZero();
  }
}
