Feature: Http SSL support

  Background:
    Given HTTP server SSL keystore path classpath:keystore/server.jks
    Given HTTP server SSL keystore password secr3t
    Given create HTTP server "secureHttpServer" with configuration
    | secure     | true |
    | securePort | 8443 |
    | timeout    | 1000 |
    And start HTTP server

  Scenario: Secure Http GET
    Given URL: https://localhost:8443
    Given HTTP request fork mode is enabled
    When send GET /todo
    Then receive GET /todo
    And HTTP response body: No TODOs - congratulations!
    And send HTTP 201 ACCEPTED
    And expect HTTP response body: No TODOs - congratulations!
    Then receive HTTP 201 ACCEPTED
