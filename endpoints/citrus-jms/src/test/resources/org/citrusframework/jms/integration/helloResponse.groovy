markupBuilder.HelloResponse(xmlns: 'http://citrusframework.org/schemas/samples/HelloService.xsd'){
    MessageId('${messageId}')
    CorrelationId('${correlationId}')
    User('HelloService')
    Text('Hello ${user}')
}
