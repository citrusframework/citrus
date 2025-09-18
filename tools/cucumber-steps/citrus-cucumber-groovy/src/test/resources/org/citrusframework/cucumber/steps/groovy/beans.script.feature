Feature: Beans script

  Scenario: Bean configuration
    Given variable username is "sa"
    Given variable password is ""
    Given create configuration
    """
    configuration {
        beans {
            dataSource(org.apache.commons.dbcp2.BasicDataSource) {
                driverClassName = "org.h2.Driver"
                url = "jdbc:h2:mem:camel"
                username = "${username}"
                password = "${password}"
            }
        }
    }
    """
    When verify bean dataSource
