Feature: DocString and DataTable on one step

  Scenario: Step receives a DocString and a DataTable
    Given message
      """
      Hello Citrus
      """
      | operation | greeting |
      | language  | en       |
    Then message body is Hello Citrus
    And message header operation is greeting
    And message header language is en
