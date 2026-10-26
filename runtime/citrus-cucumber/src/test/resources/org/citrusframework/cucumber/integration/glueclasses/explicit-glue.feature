Feature: Explicit glue classes

  Scenario: Run steps from an explicitly registered glue class
    Given explicit glue variable greeting is Hello Citrus
    Then explicit glue variable greeting should be Hello Citrus
