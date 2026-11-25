@reports
Feature: Traceability report built from the service's own read model
  As a quality manager
  I want consolidated evidence for a period
  So that audits do not require transcribing readings and incidents

  Background:
    Given a quality manager "David" of organization 1 with report permission

  @US12 @US47
  Scenario: Duplicate reading deliveries do not inflate the aggregates
    When the reading 1 of asset 1 at 6 degrees is delivered twice and then redelivered with a new event id
    Then the read model counts 1 reading and 1 temperature sample

  @US14 @US41
  Scenario: The report consolidates readings, incidents and responsible users
    Given the reading 1 of asset "Cold room" at 6 degrees on "2026-10-09T21:00:00Z"
    When "David" requests a "Traceability" report from "2026-10-09T20:00:00Z" to "2026-10-09T22:00:00Z" with incident 9 handled by "David"
    And the pending reports are generated
    Then the report contains 1 reading and 1 incident assigned to "David"

  @US14 @US17
  Scenario: Another organization cannot obtain the report result
    Given the reading 1 of asset "Cold room" at 6 degrees on "2026-10-09T21:00:00Z"
    When "David" requests a "Traceability" report from "2026-10-09T20:00:00Z" to "2026-10-09T22:00:00Z" with incident 9 handled by "David"
    And the pending reports are generated
    Then organization 2 cannot read the report result
