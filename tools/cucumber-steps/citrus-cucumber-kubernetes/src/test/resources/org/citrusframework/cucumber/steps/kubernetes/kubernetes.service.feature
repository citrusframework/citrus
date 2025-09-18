Feature: Kubernetes service

  Background:
    Given Kubernetes namespace service-example
    And Kubernetes service "hello-service"
    And Kubernetes service port 8800

  Scenario: Create service
    Given create Kubernetes service
    Then verify Kubernetes service hello-service exists

  Scenario: Create service
    Given create Kubernetes service hello-service-1 with target port 8881
    Then verify Kubernetes service hello-service-1 exists

  Scenario: Create service with port mapping
    Given create Kubernetes service http-service-2 with port mapping 80:8882
    Then verify Kubernetes service http-service-2 exists

  Scenario: Create service with port mappings
    Given create Kubernetes service http-service-3 with port mappings
    | 80   | 8883 |
    | 8088 | 8088 |
    Then verify Kubernetes service http-service-3 exists

  Scenario: Create service with variables
    Given variable port="80"
    Given variable targetPort="citrus:concat(1, citrus:randomNumber(4))"
    Given create Kubernetes service http-service-4 with port mapping ${port}:${targetPort}
    Then verify Kubernetes service http-service-4 exists
