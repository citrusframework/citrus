Feature: Kubernetes pod

  Background:
    Given Kubernetes namespace pod-example
    Given Kubernetes resource polling configuration
      | maxAttempts          | 10   |
      | delayBetweenAttempts | 1000 |

  Scenario: Verify pod running
    Given Kubernetes pod p1
    Then Kubernetes pod p1 should be running

  Scenario: Verify pod stopped
    Given Kubernetes pod p2 in phase Stopped
    Then Kubernetes pod p2 should be stopped

  Scenario: Find pod by label
    Given Kubernetes pod p3 with label citrusframework.org/pod=sample
    Then Kubernetes pod labeled with citrusframework.org/pod=sample should be running
