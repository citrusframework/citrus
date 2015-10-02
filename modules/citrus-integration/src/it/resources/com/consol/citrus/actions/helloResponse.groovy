markupBuilder.HelloResponse(xmlns: 'http://www.consol.de/schemas/samples/sayHello.xsd'){
    MessageId('${messageId}')
    CorrelationId('${correlationId}')
    User('HelloService')
    Text('Hello ${user}')
}