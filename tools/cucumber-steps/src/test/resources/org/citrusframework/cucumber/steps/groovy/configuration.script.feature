Feature: Configuration script

  Scenario: Inline config
    Given URL: http://localhost:18080
    Given create configuration
    """
    configuration {
        endpoints {
            http {
                server('helloServer') {
                    port = 18080
                    autoStart = true
                }
            }
        }
    }
    """
    When verify endpoint helloServer
    Then send GET /hello
    And receive HTTP 200 OK

  Scenario: Config file resource
    Given load configuration citrus.configuration.groovy
    When verify endpoint hello
    When endpoint hello sends body Hello from new direct endpoint!
    Then endpoint hello should receive body Hello from new direct endpoint!
