@component @trainer
Feature: Trainer profile management
  Trainers register publicly with a specialization taken from the fixed list of
  training types; profiles are readable by any authenticated user.

  Scenario: Successful trainer registration returns generated credentials
    When I register a trainer with first name "Serg" and last name "Kim" specialized in "YOGA"
    Then the response status should be 201
    And the response should contain generated credentials

  Scenario: Trainer registration with an unknown specialization is rejected
    When I register a trainer with first name "Alla" and last name "Ivy" specialized in "KUNGFU"
    Then the response status should be 404
    And the response should contain error "Training type not found: KUNGFU"

  Scenario: An authenticated user can view a trainer profile
    Given a registered trainer "Petr" "Holt" specialized in "FITNESS"
    And a registered trainee "Rita" "Moss"
    And I am logged in as "Rita" "Moss"
    When I request the trainer profile of "Petr" "Holt"
    Then the response status should be 200
    And the response should contain "FITNESS"

  Scenario: Requesting a trainer profile without a token is unauthorized
    Given a registered trainer "Karl" "Webb" specialized in "ZUMBA"
    When I request the trainer profile of "Karl" "Webb" without a token
    Then the response status should be 401

  Scenario: Training types are available to authenticated users
    Given a registered trainee "Tia" "Long"
    And I am logged in as "Tia" "Long"
    When I request the list of training types
    Then the response status should be 200
    And the response should contain "FITNESS"
    And the response should contain "YOGA"
