Feature: Kubernetes secrets

  Background:
    Given Kubernetes namespace secret-example

  Scenario: Create secret
    Given create Kubernetes secret my-secret
    | username | admin  |
    | password | secret |
    Then verify secret my-secret exists
    Then create label app=citrus on Kubernetes secret my-secret
    Then create annotation app=citrus on Kubernetes secret my-secret

  Scenario: Create from file resource
    Given load Kubernetes secret from file secret.properties
    Then verify secret secret exists
    Then delete Kubernetes secret secret
