Feature: Petstore API V3

  Background:
    Given OpenAPI specification: http://localhost:8680/petstore/v3/openapi.json
    Given variable petId is "citrus:randomNumber(5)"
    Given OpenAPI inbound dictionary
      | $.name          | @assertThat(anyOf(is(hasso),is(cutie),is(fluffy)))@ |
      | $.category.name | @assertThat(anyOf(is(dog),is(cat),is(fish)))@ |
    Given OpenAPI outbound dictionary
      | $.name          | citrus:randomEnumValue('hasso','cutie','fluffy') |
      | $.category.name | citrus:randomEnumValue('dog', 'cat', 'fish') |

  Scenario: getPet
    When invoke operation: getPetById
    Then verify operation result: 200 OK

  Scenario: getPet verbose
    Given variable verbose is "true"
    When invoke operation: getPetById
    Then verify operation result: 200 OK

  Scenario: petNotFound
    Given variable petId is "0"
    When invoke operation: getPetById
    Then verify operation result: 404 NOT_FOUND

  Scenario: addPet
    When invoke operation: addPet
    Then verify operation result: 201 CREATED

  Scenario: updatePet
    When invoke operation: updatePet
    Then verify operation result: 200 OK

  Scenario: deletePet
    When invoke operation: deletePet
    Then verify operation result: 204 NO_CONTENT
