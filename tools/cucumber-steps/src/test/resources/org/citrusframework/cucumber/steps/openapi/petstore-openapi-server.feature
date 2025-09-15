Feature: OpenAPI server

  Background:
    Given OpenAPI specification: org/citrusframework/cucumber/steps/openapi/petstore-v3.json
    Given OpenAPI service port 8681
    Given URL: http://localhost:8681/petstore/v3
    Given HTTP request fork mode is enabled
    Given OpenAPI outbound dictionary
      | $.id            | ${petId} |
      | $.name          | fluffy |
      | $.category.name | cat |
      | $.status        | available |
      | $.photoUrls[0]  | http://localhost:8681/photos/${petId} |
      | $.tags[0].name  | generated |
    Given create OpenAPI service

  Scenario: getPet
    Given variable petId is "1000"
    When send GET /pet/${petId}
    Then verify operation: getPetById
    And send operation response: 200
    Then verify HTTP response body
    """
    {
      "id": ${petId},
      "name": "fluffy",
      "category": {
        "id": "@ignore@",
        "name":"cat"
      },
      "status": "available",
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
    Then verify operation: getPetById
    And send operation response: 404
    And receive HTTP 404 NOT_FOUND

  Scenario: addPet
    Given Disable OpenAPI validate optional fields
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
    Given HTTP request header Content-Type="application/json"
    When send POST /pet
    Then verify operation: addPet
    And send operation response: 201
    Then receive HTTP 201 CREATED
    Then Enable OpenAPI validate optional fields

  Scenario: addPet with YAML spec
    Given OpenAPI specification: org/citrusframework/cucumber/steps/openapi/petstore-v3.yaml
    Given HTTP request body
    """
    {
      "id": 1,
      "name": "fluffy",
      "category": {
        "id": "1",
        "name":"cat"
      },
      "status": "available",
      "photoUrls":[
        "http://localhost:8681/photos/1"
      ],
      "tags":[
        {
          "id": "1",
          "name":"generated"
        }
      ]
    }
    """
    Given HTTP request header Content-Type="application/json"
    When send POST /pet
    Then verify operation: addPet
    And send operation response: 201
    Then receive HTTP 201 CREATED
    Then Enable OpenAPI validate optional fields
