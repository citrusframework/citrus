Feature: Http client

  Background:
    Given variable port is "8085"
    Given URL: http://localhost:${port}

  Scenario: Health check
    Given URL is healthy
    And URL http://localhost:${port}/todo is healthy
    And path /todo is healthy

  Scenario: Wait for Http URL condition
    Given HTTP request timeout is 5000 milliseconds
    Then wait for URL http://localhost:${port}/todo to return 200 OK
    And wait for path /todo to return 200 OK
    And wait for GET on URL http://localhost:${port}/todo to return 200 OK
    And wait for GET on URL /todo to return 200 OK
    And wait for GET on URL http://localhost:${port}/todo
    And wait for GET on path /todo

  Scenario: GET
    When send GET /todo
    Then verify HTTP response body: {"id": "@ignore@", "task": "Sample task", "completed": 0}
    And receive HTTP 200 OK

  Scenario: POST
    Given variable id is "citrus:randomNumber(5)"
    Given HTTP request body
    """
    {"id": "${id}", "task": "New task", "completed": 0}
    """
    When send POST /todo/${id}
    Then receive HTTP 201 CREATED

  Scenario: DELETE
    Given variable id is "citrus:randomNumber(5)"
    When send DELETE /todo/${id}
    Then receive HTTP 204 NO_CONTENT

  Scenario: PUT
    Given variable id is "citrus:randomNumber(5)"
    Given HTTP request body
    """
    {"id": "${id}", "task": "Task update", "completed": 0}
    """
    When send PUT /todo/${id}
    And verify HTTP response body
    """
    {"id": "${id}", "task": "Task update", "completed": 0}
    """
    Then receive HTTP 200 OK

  Scenario: Request header
    Given HTTP request header Accept is "application/json"
    Given HTTP request header Accept-Encoding="gzip"
    When send GET /todo
    Then receive HTTP 200 OK

  Scenario: Request headers
    Given HTTP request headers
      | Accept          | application/json |
      | Accept-Encoding | gzip |
    When send GET /todo
    Then receive HTTP 200 OK

  Scenario: Verify response header
    When send GET /todo
    Then verify HTTP response header X-TodoId is "@isNumber()@"
    And verify HTTP response header Content-Type="application/json"
    And receive HTTP 200 OK

  Scenario: Verify response headers
    When send GET /todo
    Then verify HTTP response headers
      | X-TodoId      | @isNumber()@ |
      | Content-Type  | application/json |
    And receive HTTP 200 OK

  Scenario: Verify multiline response body
    When send GET /todo
    Then verify HTTP response body
    """
    {
      "id": "@ignore@",
      "task": "Sample task",
      "completed": 0
    }
    """
    And receive HTTP 200 OK

  Scenario: Load request body
    Given variable id is "citrus:randomNumber(5)"
    And load HTTP request body task.json
    When send POST /todo/${id}
    Then receive HTTP 201 CREATED
    When send GET /todo/${id}
    Then verify HTTP response body loaded from task.json
    And receive HTTP 200 OK

  Scenario: Verify RAW data
    Given variable id is "citrus:randomNumber(5)"
    When send HTTP request
"""
GET http://localhost:8080/todo
Accept-Charset:utf-8
Accept:application/json, application/*+json, */*
Host:localhost:8080
Content-Type:text/plain;charset=UTF-8
"""
    Then receive HTTP response
"""
HTTP/1.1 200 OK
Content-Type:application/json
X-TodoId:@isNumber()@
Date: @ignore@

{"id": "@ignore@", "task": "Sample task", "completed": 0}
"""

  Scenario: Verify JsonPath expression
    When send GET /todo
    Then verify HTTP response expression: $.task is "Sample task"
    And receive HTTP 200 OK

  Scenario: Verify JsonPath expressions
    When send GET /todo
    Then verify HTTP response expressions
      | $.id        | @isNumber()@ |
      | $.task      | Sample task |
      | $.completed | 0 |
    And receive HTTP 200 OK
