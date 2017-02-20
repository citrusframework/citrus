Feature: Echo designer features

  Scenario: Echo messages
    Given variable foo is "bar"
    Then echo "Variable foo=${foo}"
    Then echo "Today is citrus:currentDate()"