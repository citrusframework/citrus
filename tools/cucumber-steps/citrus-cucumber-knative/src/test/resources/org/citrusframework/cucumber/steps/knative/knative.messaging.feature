Feature: Knative messaging

  Background:
    Given Disable auto removal of Knative resources
    Given Knative namespace event-example

  Scenario: Create channel
    Given create Knative channel my-channel
    Then verify Knative channel my-channel exists

  Scenario: Create subscription
    Given subscribe service hello-service to Knative channel my-channel
    Then verify service hello-service subscribes to Knative channel my-channel
