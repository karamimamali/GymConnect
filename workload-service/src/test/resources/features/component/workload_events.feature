@component @events
Feature: Workload event processing
  ADD events accrue training minutes into per-year/per-month buckets; DELETE
  events reverse them. Events missing required information are parked on the
  application dead letter queue instead of being retried forever.

  Background:
    Given the dead letter queue is empty
    And I have a service token issued with the shared secret

  Scenario: ADD events for the same month accumulate
    When an "ADD" workload event of 60 minutes on "2026-05-10" is received for trainer "Anna.Lee"
    And an "ADD" workload event of 30 minutes on "2026-05-20" is received for trainer "Anna.Lee"
    And all workload events have been processed
    And I request the workload summary of trainer "Anna.Lee"
    Then the response status should be 200
    And the summary should show 90 minutes for month 5 of year 2026

  Scenario: Events in different months are bucketed separately
    When an "ADD" workload event of 60 minutes on "2026-05-10" is received for trainer "Nora.Beck"
    And an "ADD" workload event of 45 minutes on "2026-06-01" is received for trainer "Nora.Beck"
    And all workload events have been processed
    And I request the workload summary of trainer "Nora.Beck"
    Then the response status should be 200
    And the summary should show 60 minutes for month 5 of year 2026
    And the summary should show 45 minutes for month 6 of year 2026

  Scenario: A DELETE event reverses a previous ADD and prunes empty buckets
    When an "ADD" workload event of 60 minutes on "2026-05-10" is received for trainer "Omar.Diaz"
    And a "DELETE" workload event of 60 minutes on "2026-05-10" is received for trainer "Omar.Diaz"
    And all workload events have been processed
    And I request the workload summary of trainer "Omar.Diaz"
    Then the response status should be 200
    And the summary should have no recorded years

  Scenario: A DELETE event for an unknown trainer is ignored
    When a "DELETE" workload event of 60 minutes on "2026-05-10" is received for trainer "Ghost.Trainer"
    And all workload events have been processed
    And I request the workload summary of trainer "Ghost.Trainer"
    Then the response status should be 404

  Scenario: An event missing required information is moved to the dead letter queue
    When a workload event with a blank username is received
    Then the event should be moved to the dead letter queue with a reason containing "Trainer username is required"
