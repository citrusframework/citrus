Feature: Birthday service

  Scenario: Birthday wishes
    Given It is my 50. birthday
    When I go to welcome page
    Then I will get best wishes to my 50. birthday

  Scenario: Say hello XML
    Given My name is Citrus
    When I say hello to the service
    Then the service should return: "Hello, my name is Citrus!"

  Scenario: Say goodbye XML
    Given My name is Citrus
    When I say goodbye to the service
    Then the service should return: "Goodbye from Citrus!"