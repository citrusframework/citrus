package com.consol.citrus.admin.converter;

import com.consol.citrus.admin.model.MessageReceiverType;
import com.consol.citrus.model.config.core.JmsMessageReceiver;

/**
 * @author Christoph Deppisch
 */
public class JmsMessageReceiverConverter implements MessageReceiverConverter<JmsMessageReceiver> {

    @Override
    public MessageReceiverType convert(JmsMessageReceiver definition) {
        MessageReceiverType messageReceiverType = new com.consol.citrus.admin.model.ObjectFactory().createMessageReceiverType();

        messageReceiverType.setName(definition.getId());
        messageReceiverType.setDestination(definition.getDestinationName());
        messageReceiverType.setType("JMS");

        return messageReceiverType;
    }
}
