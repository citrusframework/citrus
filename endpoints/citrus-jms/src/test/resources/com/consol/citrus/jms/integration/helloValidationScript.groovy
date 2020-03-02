assert root.children().size() == 4
assert root.MessageId.text() == '${messageId}'
assert root.CorrelationId.text() == '${correlationId}'
assert root.User.text() == 'HelloService'
assert root.Text.text() == 'Hello ' + context.getVariable("user")
