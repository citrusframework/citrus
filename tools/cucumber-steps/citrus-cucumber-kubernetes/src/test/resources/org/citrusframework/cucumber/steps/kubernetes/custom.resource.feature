Feature: Kubernetes custom resource

  Background:
    Given Kubernetes namespace crd-example
    Given Kubernetes resource polling configuration
      | maxAttempts          | 10   |
      | delayBetweenAttempts | 1000 |

  Scenario: Create custom resource
    Given create Kubernetes custom resource in brokers.eventing.knative.dev
"""
apiVersion: eventing.knative.dev/v1
kind: Broker
metadata:
  name: my-broker
"""
    Then verify broker my-broker exists

  Scenario: Create from file resource
    Given load Kubernetes custom resource broker.yaml in brokers.eventing.knative.dev
    Then verify broker my-broker-resource exists

  Scenario: Wait for custom resource
    Given create Kubernetes custom resource in foos.citrus.dev
"""
apiVersion: citrus.dev/v1
kind: Foo
metadata:
  name: test-resource
spec:
  message: Hello
status:
  conditions:
  - type: Ready
    status: true
"""
    Then wait for condition=Ready on Kubernetes custom resource foo/test-resource in foos.citrus.dev/v1
    Then wait for condition=Ready on Kubernetes custom resource
      | apiVersion | citrus.dev/v1 |
      | kind       | Foo         |
      | name       | test-resource |
    Then Kubernetes custom resource foo/test-resource in foos.citrus.dev/v1 should be ready
    Then Kubernetes custom resource test-resource should be ready
      | group   | citrus.dev |
      | version | v1       |
      | kind    | Foo      |

  Scenario: Wait for labeled custom resource
    Given create Kubernetes custom resource in foos.citrus.dev
"""
apiVersion: citrus.dev/v1
kind: Foo
metadata:
  name: bar-resource
  labels:
    app: foo-app
spec:
  message: Hello
status:
  conditions:
  - type: Ready
    status: true
"""
    Then wait for condition=Ready on Kubernetes custom resource Foo in foos.citrus.dev/v1 labeled with app=foo-app
    Then wait for condition=Ready on Kubernetes custom resource
      | apiVersion | citrus.dev/v1 |
      | kind       | Foo         |
      | label      | app=foo-app |
    Then Kubernetes custom resource Foo in foos.citrus.dev/v1 labeled with app=foo-app should be ready
    Then Kubernetes custom resource should be ready
      | apiVersion | citrus.dev/v1 |
      | kind       | Foo         |
      | label      | app=foo-app |

  Scenario: Wait for completed custom resource
    Given create Kubernetes custom resource in foos.citrus.dev
"""
apiVersion: citrus.dev/v1
kind: Foo
metadata:
  name: job-resource
spec:
  message: Hello
status:
  conditions:
  - type: Completed
    status: true
"""
    Then wait for condition=Completed on Kubernetes custom resource foo/job-resource in foos.citrus.dev/v1
