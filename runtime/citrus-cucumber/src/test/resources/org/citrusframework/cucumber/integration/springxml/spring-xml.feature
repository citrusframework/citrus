Feature: XML step templates with the Citrus Spring object factory

  Scenario: Java and XML steps share the test context
    Given Spring XML user is Citrus
    When the XML step greets with Hello
    Then Spring XML greeting is Hello Citrus
