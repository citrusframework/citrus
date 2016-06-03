Feature: Core features

Background:
    Given variable text is "Hello"

  Scenario: Send and receive
    When <fooEndpoint> sends
      """
      <message>
        <text>${text}</text>
      </message>
      """
    Then <fooEndpoint> should receive
      """
      <message>
        <text>${text}</text>
      </message>
      """

  Scenario: Send and receive plaintext
    When <echoEndpoint> sends "${text}"
    Then <echoEndpoint> should receive plaintext "You just said: ${text}"

  Scenario: Send and receive plaintext
    When <echoEndpoint> sends
      """
      ${text}
      """
    Then <echoEndpoint> should receive plaintext
      """
      You just said: ${text}
      """

  Scenario: Message header validation
    When <echoEndpoint> sends "${text}"
    And message header operation is "sayHello"
    Then <echoEndpoint> should receive plaintext "You just said: ${text}"
    And message header operation should be "sayHello"