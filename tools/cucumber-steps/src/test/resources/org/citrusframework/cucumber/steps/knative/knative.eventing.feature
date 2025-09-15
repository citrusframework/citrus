Feature: Knative eventing

  Background:
    Given Disable auto removal of Knative resources
    Given Knative namespace event-example
    And Knative broker my-broker

  Scenario: Create broker
    Given create Knative broker my-broker
    When activate Knative broker my-broker
    Then Knative broker my-broker is running

  Scenario: Create service and trigger
    Given Knative namespace trigger
    Given create Knative event consumer service hello-service with target port 8080
    Given create Knative trigger hello-trigger on service hello-service
    Then verify Knative trigger hello-trigger exists

  Scenario: Create trigger with filter
    Given Knative namespace trigger-with-filter
    Given create Knative trigger filtered-trigger on service hello-service with filter on attributes
      | app    | citrus |
      | source | testing |
    Then verify Knative trigger filtered-trigger exists with filter
      | app    | citrus |
      | source | testing |


