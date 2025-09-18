Feature: REST API

  Background:
    Given URL: http://localhost:8680/petstore/v2

  Scenario: getPet
    Given variable petId is "1000"
    When send GET /pet/${petId}
    Then verify HTTP response body
    """
    {
      "id": ${petId},
      "name": "@matches(cutie|fluffy|hasso)@",
      "category": {
        "id": "@ignore@",
        "name":"@matches(dog|cat|fish)@"
      },
      "status": "@matches(available|pending|sold)@",
      "photoUrls":[
        "http://localhost:8681/photos/${petId}"
      ],
      "tags":[
        {
          "id": "@ignore@",
          "name":"generated"
        }
      ]
    }
    """
    And verify HTTP response header Content-Type="application/json"
    And receive HTTP 200 OK

  Scenario: petNotFound
    Given variable petId is "0"
    When send GET /pet/${petId}
    And receive HTTP 404 NOT_FOUND

  Scenario: addPet
    Given HTTP request body
    """
    {
      "name": "hasso",
      "category":{
        "id": 1,
        "name":"dog"
      },
      "status": "available"
    }
    """
    When send POST /pet
    Then receive HTTP 201 CREATED
