Feature: Knative event producer

  Background:
    Given variable port is "8188"
    Given Knative event producer timeout is 5000 ms
    Given Knative broker URL: http://localhost:${port}

  Scenario: Send event
    When send Knative event
      | specversion     | 1.0 |
      | type            | greeting |
      | source          | https://github.com/citrusframework |
      | subject         | hello |
      | id              | say-hello |
      | time            | citrus:currentDate('yyyy-MM-dd'T'HH:mm:ss') |
      | datacontenttype | application/json |
      | data            | {"msg": "Hello Knative!"} |

  Scenario: Send event data
    Given Knative event data: {"msg": "Hello Knative!"}
    When send Knative event
      | type            | greeting |
      | source          | https://github.com/citrusframework |
      | subject         | hello |
      | id              | say-hello |

  Scenario: Send event http
    Given Knative event data: {"msg": "Hello Knative!"}
    When send Knative event
      | ce-specversion     | 1.0 |
      | ce-type            | greeting |
      | ce-source          | https://github.com/citrusframework |
      | ce-subject         | hello |
      | ce-id              | say-hello |
      | ce-time            | citrus:currentDate('yyyy-MM-dd'T'HH:mm:ss') |
      | Content-Type       | application/json |

  Scenario: Send multiline event data
    Given Knative event data
    """
    {"msg": "Hello Knative!"}
    """
    When send Knative event
      | type            | greeting |
      | source          | https://github.com/citrusframework |
      | subject         | hello |
      | id              | say-hello |

  Scenario: Send event json
    When send Knative event as json
    """
    {
      "specversion" : "1.0",
      "type" : "greeting",
      "source" : "https://github.com/citrusframework",
      "subject" : "hello",
      "id" : "say-hello",
      "time" : "citrus:currentDate('yyyy-MM-dd'T'HH:mm:ss')",
      "datacontenttype" : "application/json",
      "data" : "{\"msg\": \"Hello Knative!\"}"
    }
    """
