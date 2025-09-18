Feature: Http Basic Auth support

  Scenario: Start Http server
    Given create HTTP server "basicAuthHttpServer" with configuration
    | port         | 8081         |
    | authMethod   | basic        |
    | authUser     | citrus_user  |
    | authPassword | secr3t       |
    | authPath     | /auth/*      |
    And start HTTP server

  Scenario: Basic Auth Http GET
    Given HTTP request header Authorization="Basic citrus:encodeBase64(citrus_user:secr3t)"
    Given URL: http://localhost:8081
    When send GET /auth
    Then receive HTTP 200 OK

  Scenario: Basic Auth Http client
    Given URL: http://localhost:8081
    Given HTTP client auth method basic
    Given HTTP client auth user citrus_user
    Given HTTP client auth password secr3t
    When send GET /auth
    Then receive HTTP 200 OK

  Scenario: Basic Auth Error
    Given URL: http://localhost:8081
    When send GET /auth
    Then receive HTTP 401 UNAUTHORIZED
