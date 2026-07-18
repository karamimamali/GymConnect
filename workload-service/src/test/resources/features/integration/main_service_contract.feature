@integration @contract
Feature: Integration contract with the main service
  The two microservices integrate asynchronously over an ActiveMQ queue and
  authenticate service-to-service REST calls with a shared JWT secret. These
  scenarios feed the workload service the exact wire format the main service
  produces, so a contract break on either side fails the build.

  Scenario: The exact message published by the main service is consumed and applied
    When the following message arrives on the workload queue with type id "TrainerWorkloadMessage":
      """
      {"username":"Leo.Marsh","firstName":"Leo","lastName":"Marsh","active":true,"trainingDate":"2026-07-15","trainingDuration":90,"actionType":"ADD"}
      """
    And all workload events have been processed
    And I have a service token issued with the shared secret
    And I request the workload summary of trainer "Leo.Marsh"
    Then the response status should be 200
    And the summary should be for trainer "Leo.Marsh" named "Leo" "Marsh"
    And the summary should show 90 minutes for month 7 of year 2026

  Scenario: A caller without the shared secret cannot read summaries
    Given I have a service token signed with an unknown key
    When I request the workload summary of trainer "Leo.Marsh"
    Then the response status should be 401
