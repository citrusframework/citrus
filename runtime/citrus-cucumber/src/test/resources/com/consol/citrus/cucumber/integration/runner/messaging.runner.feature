Feature: Messaging runner features

Background:
  Given variable text is "Hello"

  Scenario: Send and receive inline
    When <fooEndpoint> sends "<message><text>${text}</text></message>"
    Then <fooEndpoint> should receive "<message><text>${text}</text></message>"

  Scenario: Send and receive multiline
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

  Scenario: Send and receive plaintext inline
    When <echoEndpoint> sends "${text}"
    Then <echoEndpoint> should receive plaintext "You just said: ${text}"

  Scenario: Send and receive plaintext multiline
    When <echoEndpoint> sends
      """
      ${text}
      """
    Then <echoEndpoint> should receive plaintext
      """
      You just said: ${text}
      """

  Scenario: Message definition inline
    Given message echoRequest
      And <echoRequest> payload is "${text}"
      And <echoRequest> header operation is "sayHello"
    Given message echoResponse
      And <echoResponse> payload is "You just said: ${text}"
      And <echoResponse> header operation is "sayHello"
    When <echoEndpoint> sends message <echoRequest>
    Then <echoEndpoint> should receive plaintext message <echoResponse>

  Scenario: Message definition multiline
    Given message echoRequest
      And <echoRequest> payload is
        """
        ${text}
        """
      And <echoRequest> header operation is "sayHello"
    Given message echoResponse
      And <echoResponse> payload is
        """
        You just said: ${text}
        """
      And <echoResponse> header operation is "sayHello"
    When <echoEndpoint> sends message <echoRequest>
    Then <echoEndpoint> should receive plaintext message <echoResponse>