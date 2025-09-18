Feature: Run script actions

  Scenario: Load actions
    Given load actions actions.groovy
    Then apply actions actions.groovy
    And variable greeting is "Hey there"

  Scenario: Apply actions file
    Given variable greeting is "Hey there"
    Then apply actions echo.groovy

  Scenario: Inline actions
    Given create actions basic.groovy
    """
    $actions {
      $(doFinally().actions(
          echo('${greeting} in finally!')
      ))

      $(echo('Hello from Groovy script'))
      $(delay().seconds(1))

      $(createVariables()
          .variable('foo', 'bar'))

      $(echo('Variable foo=${foo}'))
    }
    """
    Then apply actions basic.groovy
    And variable greeting is "Hello"

  Scenario: Messaging actions
    Given create actions messaging.groovy
    """
    $actions {
      $(send('direct:myQueue')
        .message()
        .body('Hello from Groovy script!'))

      $(receive('direct:myQueue')
        .message()
        .body('Hello from Groovy script!'))
    }
    """
    Then apply actions messaging.groovy

  Scenario: Run actions
    Given $(doFinally().actions(echo('${greeting} in finally!')))
    When $(createVariables().variable('greeting', 'Hello from Citrus!'))
    Then $(echo('${greeting}'))
    And print '${greeting}'
    And variable greeting is "Ciao"
    And $(echo(greeting))

  Scenario: Run actions multiline
    Given apply script
    """
    $(doFinally().actions(
        echo('${greeting} in finally!')
    ))
    """
    When apply script
    """
    $(send('direct:myQueue')
      .message()
      .body('Hello from Groovy script!'))
    """
    Then apply script
    """
    $(receive('direct:myQueue')
      .message()
      .body('Hello from Groovy script!'))
    """
    And variable greeting is "Bye"
