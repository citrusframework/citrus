Feature: Message creators

Background:
  Given create message queue foo
  Given message creator types
    | org.citrusframework.cucumber.steps.core.message.BarMessageCreator |
  Given message creator type org.citrusframework.cucumber.steps.core.message.PojoMessageCreator

  Scenario: Message creator as bean
    When endpoint fooEndpoint sends message $fooMessage
    Then endpoint fooEndpoint should receive message $fooMessage

  Scenario: Message creator by type
    When endpoint fooEndpoint sends message $BarMessageCreator
    Then endpoint fooEndpoint should receive message $BarMessageCreator

  Scenario: Pojo message creator
    When endpoint echoEndpoint sends message $pojoRequest
    Then endpoint echoEndpoint should receive message $pojoResponse


