Feature: Kubernetes resource

  Background:
    Given Kubernetes namespace crd-example

  Scenario: Create resource
    Given create Kubernetes resource
"""
apiVersion: v1
kind: Pod
metadata:
  name: my-pod
  labels:
    name: my-pod
spec:
  containers:
  - name: nginx
    image: nginx
    ports:
    - containerPort: 80
"""
    Then verify pod my-pod exists

  Scenario: Create from file resource
    Given load Kubernetes resource pod.yaml
    Then verify pod my-pod-resource exists
