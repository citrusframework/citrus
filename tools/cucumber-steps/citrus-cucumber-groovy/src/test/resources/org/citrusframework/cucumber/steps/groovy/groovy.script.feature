Feature: Run script

  Scenario: Apply script file
    Given variable greeting is "Hey there"
    Then run script hello.groovy

  Scenario: Inline script
    Given variable greeting is "Hello"
    Given run script
    """
    println '${greeting} from Groovy script!'
    """
