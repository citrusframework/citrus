Feature: Selenium feature

  Background:
    Given start browser

  Scenario: Index page
    Given user navigates to "http://localhost:8780/"
    And browser page should display heading with tag-name="h1" having
    | text   | Welcome!       |
    | styles | font-size=40px |
    And browser page should display element with id="hello-text" having
    | text   | Hello!         |
    | styles | background-color=rgba(0, 0, 0, 0) |
    When user clicks element with id="open-alert"
    And sleep 500 ms
    Then browser page should display alert with text "Hello"

  Scenario: User form page
    Given browser page "userForm"
    Given user navigates to "form"
    When browser page userForm performs setUserName with arguments
    | Christoph |
    Then browser page userForm should validate
    And browser page userForm performs submit
