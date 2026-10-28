Feature: Citrus suite hooks without the Citrus reporter

  Scenario: Suite finishes through the after-all hook
    Given suite hooks scenario runs
    Then the Citrus suite has not finished yet
