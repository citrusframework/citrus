Feature: cucumber-sample.citrus.it

  Background:
    Given variables
      | message | "Citrus rocks! With Cucumber BDD!" |

  Scenario: Print message
    Then print '${message}'
