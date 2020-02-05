package com.consol.citrus.validation.interceptor;

import java.io.IOException;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageDirection;
import com.consol.citrus.message.MessageType;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

/**
 * Message construction interceptor automatically converts message payloads to binary content. Supports String typed message payloads and
 * payload resources.
 *
 * @author Christoph Deppisch
 */
public class BinaryMessageConstructionInterceptor extends AbstractMessageConstructionInterceptor {

    @Override
    protected Message interceptMessage(Message message, String messageType, TestContext context) {
        if (message.getPayload() instanceof String) {
            message.setPayload(message.getPayload(String.class).getBytes());
        } else if (message.getPayload() instanceof Resource) {
            try {
                message.setPayload(FileCopyUtils.copyToByteArray(message.getPayload(Resource.class).getInputStream()));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to build binary message payload from payload resource", e);
            }
        }

        return message;
    }

    @Override
    public boolean supportsMessageType(String messageType) {
        return MessageType.BINARY.name().equalsIgnoreCase(messageType);
    }

    @Override
    public MessageDirection getDirection() {
        return MessageDirection.OUTBOUND;
    }
}
