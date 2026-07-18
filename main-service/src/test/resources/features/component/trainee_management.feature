@component @trainee
Feature: Trainee profile management
  Registration is public; every other trainee operation requires a valid JWT.

  Scenario: Successful trainee registration returns generated credentials
    When I register a trainee with first name "Elena" and last name "Petrova"
    Then the response status should be 201
    And the response should contain generated credentials

  Scenario: Trainee registration without a first name is rejected
    When I register a trainee with first name "" and last name "Petrova"
    Then the response status should be 400
    And the response should contain error "First name is required"

  Scenario: An authenticated trainee can view a profile
    Given a registered trainee "Igor" "Volkov"
    And I am logged in as "Igor" "Volkov"
    When I request the trainee profile of "Igor" "Volkov"
    Then the response status should be 200
    And the response should contain "Igor"

  Scenario: Requesting an unknown trainee profile returns not found
    Given a registered trainee "Pavel" "Smirnov"
    And I am logged in as "Pavel" "Smirnov"
    When I request the trainee profile for username "No.Such.User"
    Then the response status should be 404
    And the response should contain error "Trainee not found: No.Such.User"

  Scenario: A trainee profile can be updated
    Given a registered trainee "Nina" "Orlova"
    And I am logged in as "Nina" "Orlova"
    When I update the trainee "Nina" "Orlova" with last name "Orlova-Bell" and address "12 Main St"
    Then the response status should be 200
    And the response should contain "Orlova-Bell"

  Scenario: Deactivating an already deactivated trainee is rejected
    Given a registered trainee "Boris" "Adams"
    And a registered trainee "Clara" "Dane"
    And I am logged in as "Clara" "Dane"
    When I set the active status of trainee "Boris" "Adams" to false
    Then the response status should be 200
    When I set the active status of trainee "Boris" "Adams" to false
    Then the response status should be 400
    And the response should contain error "already inactive"

  Scenario: A deactivated trainee's token is no longer accepted
    Given a registered trainee "Finn" "Cole"
    And I am logged in as "Finn" "Cole"
    When I set the active status of trainee "Finn" "Cole" to false
    Then the response status should be 200
    When I request the trainee profile of "Finn" "Cole"
    Then the response status should be 401

  Scenario: A deleted trainee profile can no longer be retrieved
    Given a registered trainee "Dana" "Frost"
    And a registered trainee "Eve" "Hart"
    And I am logged in as "Eve" "Hart"
    When I delete the trainee "Dana" "Frost"
    Then the response status should be 200
    When I request the trainee profile of "Dana" "Frost"
    Then the response status should be 404
