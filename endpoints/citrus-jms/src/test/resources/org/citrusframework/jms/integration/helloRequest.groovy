markupBuilder.HelloRequest(xmlns: 'http://citrusframework.org/schemas/samples/HelloService.xsd'){
    MessageId('${messageId}')
    CorrelationId('${correlationId}')
    User('${user}')
    Text('Hello TestFramework')
}
