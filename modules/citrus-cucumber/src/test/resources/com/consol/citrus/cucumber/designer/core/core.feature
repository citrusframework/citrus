Feature: Core features

  Scenario: Send and receive
    Given variable text="Hello"
    When <fooEndpoint> sends "<message><text>${text}</text></message>"
    Then <fooEndpoint> should receive "<message><text>${text}</text></message>"

  Scenario: Send and receive plaintext
    Given variable text="Hello"
    When <echoEndpoint> sends "${text}"
    Then <echoEndpoint> should receive "You just said: ${text}" as plaintext