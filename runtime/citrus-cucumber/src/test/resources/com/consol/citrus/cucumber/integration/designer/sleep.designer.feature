Feature: Sleep designer features

  Scenario: Sleep default time
    Given variable sleep is "true"
    Then sleep

  Scenario: Sleep milliseconds time
    Given variable time is "200"
    Then sleep 200 ms