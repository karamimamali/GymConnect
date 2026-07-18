@integration @messaging
Feature: Workload event publishing to the workload microservice
  The main service integrates with the workload microservice asynchronously:
  training ADD/DELETE events are published to the shared ActiveMQ queue as JSON
  text messages with the agreed type id, so the consumer needs none of this
  service's classes. These scenarios pin down that outbound contract.

  Background:
    Given the workload queue is empty
    And a registered trainer "Leo" "Marsh" specialized in "FITNESS"
    And a registered trainee "Zoe" "Quinn"
    And I am logged in as "Zoe" "Quinn"

  Scenario: Adding a training publishes an ADD workload event with the agreed contract
    When I add a training named "Contract session" on "2026-07-15" lasting 90 minutes for "Zoe" "Quinn" with trainer "Leo" "Marsh"
    Then the response status should be 200
    And a workload message should be published
    And the workload message should have type id "TrainerWorkloadMessage"
    And the workload message should have action "ADD", duration 90 and training date "2026-07-15"
    And the workload message should be for the trainer "Leo" "Marsh"

  Scenario: Deleting a trainee publishes a DELETE workload event for its trainings
    Given a training named "To be removed" on "2026-07-16" lasting 45 minutes exists for "Zoe" "Quinn" with trainer "Leo" "Marsh"
    And the workload queue is empty
    When I delete the trainee "Zoe" "Quinn"
    Then the response status should be 200
    And a workload message should be published
    And the workload message should have action "DELETE", duration 45 and training date "2026-07-16"
    And the workload message should be for the trainer "Leo" "Marsh"
