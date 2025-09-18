Feature: Http server

  Background:
    Given URL: http://localhost:8088
    Given HTTP server listening on port 8088
    Given create HTTP server "sampleHttpServer"
    And HTTP request fork mode is enabled

  Scenario: Http health check
    When send GET /health
    Then receive GET /health
    And send HTTP 204 NO_CONTENT
    Then receive HTTP 204 NO_CONTENT

  Scenario: Http GET
    When send GET /greetings
    Then receive GET /greetings
    And HTTP response body: ["Hello", "Hola", "Hi"]
    And send HTTP 200 OK
    And expect HTTP response body: ["Hello", "Hola", "Hi"]
    Then receive HTTP 200 OK

  Scenario: Http POST
    Given variable id is "citrus:randomNumber(5)"
    Given HTTP request body
    """
      {"id": ${id}, "message": "Hello!"}
    """
    When send POST /message/${id}
    Then expect HTTP request body
    """
      {"id": ${id}, "message": "Hello!"}
    """
    Then receive POST /message/${id}
    And send HTTP 201 CREATED
    And receive HTTP 201 CREATED
