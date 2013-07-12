package com.consol.citrus.admin.converter;

import com.consol.citrus.admin.model.MessageReceiverType;
import com.consol.citrus.model.config.core.JmsMessageReceiver;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class JmsMessageReceiverConverter implements MessageReceiverConverter<JmsMessageReceiver> {

    @Override
    public MessageReceiverType convert(JmsMessageReceiver definition) {
        MessageReceiverType messageReceiverType = new com.consol.citrus.admin.model.ObjectFactory().createMessageReceiverType();

        messageReceiverType.setName(definition.getId());

        if (StringUtils.hasText(definition.getDestinationName())) {
            messageReceiverType.setDestination(definition.getDestinationName());
        } else {
            messageReceiverType.setDestination("ref:" + definition.getDestination());
        }
        messageReceiverType.setType("JMS");

        return messageReceiverType;
    }
}
