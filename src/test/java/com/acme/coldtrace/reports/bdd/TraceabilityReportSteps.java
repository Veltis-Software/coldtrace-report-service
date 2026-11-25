package com.acme.coldtrace.reports.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.acme.coldtrace.reports.application.ReadingProjection;
import com.acme.coldtrace.reports.application.ReportJobs;
import com.acme.coldtrace.shared.IdentitySession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

@CucumberContextConfiguration
@ActiveProfiles("test")
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:reportbdd;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "coldtrace.reports.interval-ms=3600000"
    })
public class TraceabilityReportSteps {
  @Autowired ReadingProjection projection;
  @Autowired ReportJobs jobs;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate jdbc;

  private IdentitySession actor;
  private ReportJobs.Job job;

  @Given("a quality manager {string} of organization {int} with report permission")
  public void manager(String name, int organizationId) {
    for (String table :
        List.of("reports", "reading_samples", "reading_aggregates", "incident_projection", "processed_events"))
      jdbc.update("DELETE FROM " + table);
    actor = new IdentitySession(10L, (long) organizationId, name, List.of("reports:view"));
  }

  @When("the reading {int} of asset {int} at {int} degrees is delivered twice and then redelivered with a new event id")
  public void duplicateDelivery(int readingId, int assetId, int temperature) {
    JsonNode envelope =
        readings(
            Map.of(
                "readingId", readingId,
                "assetId", assetId,
                "recordedAt", "2026-10-09T21:00:00Z",
                "temperature", temperature,
                "outOfRange", false));
    projection.accept(envelope);
    projection.accept(envelope);
    ((ObjectNode) envelope).put("eventId", UUID.randomUUID().toString());
    projection.accept(envelope);
  }

  @Then("the read model counts {int} reading and {int} temperature sample")
  public void aggregates(int readings, int temperatures) {
    assertThat(jdbc.queryForObject("SELECT reading_count FROM reading_aggregates", Integer.class))
        .isEqualTo(readings);
    assertThat(jdbc.queryForObject("SELECT temperature_count FROM reading_aggregates", Integer.class))
        .isEqualTo(temperatures);
  }

  @Given("the reading {int} of asset {string} at {int} degrees on {string}")
  public void reading(int readingId, String assetName, int temperature, String recordedAt) {
    projection.accept(
        readings(
            Map.of(
                "readingId", readingId,
                "assetId", 1,
                "assetName", assetName,
                "recordedAt", recordedAt,
                "temperature", temperature)));
  }

  @When("{string} requests a {string} report from {string} to {string} with incident {int} handled by {string}")
  public void request(String user, String title, String from, String to, int incidentId, String assignee) {
    JsonNode incidents =
        json.valueToTree(
            List.of(
                Map.of(
                    "id", incidentId,
                    "asset", Map.of("assetId", 1, "name", "Cold room"),
                    "assignee", Map.of("userId", 10, "fullName", assignee),
                    "correctiveActions", List.of(Map.of("description", "Cooling restored")))));
    job = jobs.request(actor, new ReportJobs.Request(title, Instant.parse(from), Instant.parse(to)), incidents);
  }

  @And("the pending reports are generated")
  public void generate() {
    jobs.generatePending();
  }

  @Then("the report contains {int} reading and {int} incident assigned to {string}")
  public void result(int readings, int incidents, String assignee) {
    JsonNode result = jobs.result(job.id(), actor.organizationId());
    assertThat(result.path("readingCount").asInt()).isEqualTo(readings);
    assertThat(result.path("incidentCount").asInt()).isEqualTo(incidents);
    assertThat(result.path("incidents").get(0).path("assignee").path("fullName").asText())
        .isEqualTo(assignee);
  }

  @Then("organization {int} cannot read the report result")
  public void isolated(int organizationId) {
    assertThatThrownBy(() -> jobs.result(job.id(), (long) organizationId))
        .isInstanceOf(ResponseStatusException.class);
  }

  private JsonNode readings(Map<String, Object> reading) {
    return json.valueToTree(
        Map.of(
            "eventId", UUID.randomUUID(),
            "eventType", "readings.ingested",
            "version", 1,
            "organizationId", 1,
            "payload", Map.of("readings", List.of(reading))));
  }
}
