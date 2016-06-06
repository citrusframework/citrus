Feature: Variables features

Background:
    Given variables
      | hello   | I say hello   |
      | goodbye | I say goodbye |

  Scenario: Send and receive hello
    When <echoEndpoint> sends "${hello}"
    Then <echoEndpoint> should receive plaintext "You just said: ${hello}"

  Scenario: Send and receive goodbye
    When <echoEndpoint> sends "${goodbye}"
    Then <echoEndpoint> should receive plaintext "You just said: ${goodbye}"