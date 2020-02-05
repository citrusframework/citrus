package com.consol.citrus.validation.interceptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageDirection;
import com.consol.citrus.message.MessageType;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;

/**
 * Message construction interceptor automatically converts message payloads to gzipped content. Supports String typed message payloads and
 * payload resources.
 *
 * @author Christoph Deppisch
 */
public class GzipMessageConstructionInterceptor extends AbstractMessageConstructionInterceptor {

    @Override
    protected Message interceptMessage(Message message, String messageType, TestContext context) {
        try {
            if (message.getPayload() instanceof String) {
                try (ByteArrayOutputStream zipped = new ByteArrayOutputStream()) {
                    try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipped)) {
                        StreamUtils.copy(context.replaceDynamicContentInString(message.getPayload(String.class)).getBytes(), gzipOutputStream);
                    }
                    message.setPayload(zipped.toByteArray());
                }
            } else if (message.getPayload() instanceof Resource) {
                try (ByteArrayOutputStream zipped = new ByteArrayOutputStream()) {
                    try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipped)) {
                        StreamUtils.copy(FileCopyUtils.copyToByteArray(message.getPayload(Resource.class).getInputStream()), gzipOutputStream);
                    }
                    message.setPayload(zipped.toByteArray());
                }
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to gzip message payload", e);
        }

        return message;
    }

    @Override
    public boolean supportsMessageType(String messageType) {
        return MessageType.GZIP.name().equalsIgnoreCase(messageType);
    }

    @Override
    public MessageDirection getDirection() {
        return MessageDirection.OUTBOUND;
    }
}
