@component @auth @security
Feature: Authentication and access control
  As the gym CRM, the main service must issue JWT tokens on login, reject bad
  credentials, lock accounts under brute force and invalidate tokens on logout.

  Scenario: Successful login returns a JWT token
    Given a registered trainee "Anna" "Karev"
    When I login with the credentials of "Anna" "Karev"
    Then the response status should be 200
    And the response should contain a JWT token

  Scenario: Login with a wrong password is rejected
    Given a registered trainee "Mark" "Twain"
    When I login as "Mark" "Twain" with password "wrong-password"
    Then the response status should be 401
    And the response should contain error "Invalid credentials"

  Scenario: Account locks after three failed login attempts
    Given a registered trainee "Lena" "Golden"
    When I login as "Lena" "Golden" with password "bad-guess-1"
    And I login as "Lena" "Golden" with password "bad-guess-2"
    And I login as "Lena" "Golden" with password "bad-guess-3"
    And I login with the credentials of "Lena" "Golden"
    Then the response status should be 423
    And the response should contain error "Account temporarily blocked"

  Scenario: A protected endpoint rejects requests without a token
    Given a registered trainee "Olga" "Ivanova"
    When I request the trainee profile of "Olga" "Ivanova" without a token
    Then the response status should be 401
    And the response should contain error "Unauthorized"

  Scenario: A protected endpoint rejects an invalid token
    Given a registered trainee "Vera" "Stone"
    And I use an invalid token
    When I request the trainee profile of "Vera" "Stone"
    Then the response status should be 401

  Scenario: Password change works and the old password stops working
    Given a registered trainee "Omar" "Reed"
    And I am logged in as "Omar" "Reed"
    When I change the password of "Omar" "Reed" to "NewSecret123"
    Then the response status should be 200
    When I login as "Omar" "Reed" with the original password
    Then the response status should be 401
    When I login as "Omar" "Reed" with password "NewSecret123"
    Then the response status should be 200

  Scenario: Password change with a wrong old password is rejected
    Given a registered trainee "Ruth" "Miles"
    And I am logged in as "Ruth" "Miles"
    When I change the password of "Ruth" "Miles" using old password "not-the-password" to "Whatever123"
    Then the response status should be 401
    And the response should contain error "Invalid old password"

  Scenario: A logged out token is no longer accepted
    Given a registered trainee "Ivan" "Frost"
    And I am logged in as "Ivan" "Frost"
    When I logout
    Then the response status should be 200
    When I request the trainee profile of "Ivan" "Frost"
    Then the response status should be 401
