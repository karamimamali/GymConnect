@component @summary @security
Feature: Trainer workload summary retrieval
  The workload service exposes read-only monthly summaries over REST, guarded
  by the service-to-service JWT issued by the main service.

  Scenario: Requesting a summary without a token is unauthorized
    When I request the workload summary of trainer "Mike.Johnson" without a token
    Then the response status should be 401
    And the response should contain error "Unauthorized"

  Scenario: A token signed with an unknown key is rejected
    Given I have a service token signed with an unknown key
    When I request the workload summary of trainer "Mike.Johnson"
    Then the response status should be 401

  Scenario: Requesting a summary for an unknown trainer returns not found
    Given I have a service token issued with the shared secret
    When I request the workload summary of trainer "Ghost.Trainer"
    Then the response status should be 404
    And the response should contain error "No workload recorded for trainer: Ghost.Trainer"

  Scenario: An accrued summary is returned with identity and monthly totals
    When an "ADD" workload event of 60 minutes on "2026-05-10" is received for trainer "Mike.Johnson"
    And all workload events have been processed
    And I have a service token issued with the shared secret
    And I request the workload summary of trainer "Mike.Johnson"
    Then the response status should be 200
    And the summary should be for trainer "Mike.Johnson" named "Mike" "Johnson"
    And the summary should show 60 minutes for month 5 of year 2026
