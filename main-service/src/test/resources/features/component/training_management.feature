@component @training
Feature: Training management
  Trainings connect a trainee with a trainer; the training type is derived from
  the trainer's specialization.

  Background:
    Given a registered trainer "Max" "Power" specialized in "FITNESS"
    And a registered trainee "Amy" "Winter"
    And I am logged in as "Amy" "Winter"

  Scenario: Adding a training for an existing trainee and trainer succeeds
    When I add a training named "Morning cardio" on "2026-07-01" lasting 60 minutes for "Amy" "Winter" with trainer "Max" "Power"
    Then the response status should be 200
    When I request the trainings of trainee "Amy" "Winter"
    Then the response status should be 200
    And the response should contain "Morning cardio"

  Scenario: Adding a training without a duration is rejected
    When I add a training named "No duration" on "2026-07-01" without a duration for "Amy" "Winter" with trainer "Max" "Power"
    Then the response status should be 400
    And the response should contain error "Training duration is required"

  Scenario: Adding a training with an unknown trainer is rejected
    When I add a training named "Ghost session" on "2026-07-01" lasting 45 minutes for "Amy" "Winter" with trainer username "Ghost.Trainer"
    Then the response status should be 404
    And the response should contain error "Trainer not found: Ghost.Trainer"

  Scenario: Trainee trainings can be filtered by period
    When I add a training named "June session" on "2026-06-10" lasting 30 minutes for "Amy" "Winter" with trainer "Max" "Power"
    And I request the trainings of trainee "Amy" "Winter" between "2026-06-01" and "2026-06-30"
    Then the response status should be 200
    And the response should contain "June session"
    When I request the trainings of trainee "Amy" "Winter" between "2027-01-01" and "2027-12-31"
    Then the response status should be 200
    And the response should not contain "June session"
