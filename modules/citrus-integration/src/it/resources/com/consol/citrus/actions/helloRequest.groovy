markupBuilder.HelloRequest(xmlns: 'http://www.consol.de/schemas/samples/sayHello.xsd'){
    MessageId('${messageId}')
    CorrelationId('${correlationId}')
    User('${user}')
    Text('Hello TestFramework')
}