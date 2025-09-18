Feature: Run finally actions

  Scenario: Inline finally actions
    Given create actions finally.groovy
    """
    $actions {
      $(doFinally().actions(
          echo('${greeting} in finally!')
      ))

      $finally {
          echo('Greeting is ${greeting}!')
      }
    }
    """
    Then apply actions finally.groovy
    And variable greeting is "Hello"

  Scenario: Finally actions
    Given $(doFinally().actions(echo('${greeting} in finally!')))
    When $(createVariables().variable('greeting', 'Hello from Citrus!'))
    Then $(echo('${greeting}'))
    And print '${greeting}'
    And variable greeting is "Ciao"

  Scenario: Finally actions multiline
    Given apply script
    """
    $finally {
        echo('${greeting} in finally!')
    }
    """
    And variable greeting is "Bye"
