package com.consol.citrus.validation.interceptor;

import java.io.IOException;
import java.nio.charset.Charset;

import com.consol.citrus.CitrusSettings;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.AbstractMessageProcessor;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

/**
 * Message construction processor automatically converts message payloads to binary content. Supports String typed message payloads and
 * payload resources.
 *
 * @author Christoph Deppisch
 */
public class BinaryMessageProcessor extends AbstractMessageProcessor {

    private Charset encoding = Charset.forName(CitrusSettings.CITRUS_FILE_ENCODING);

    @Override
    protected Message processMessage(Message message, TestContext context) {
        if (message.getPayload() instanceof String) {
            message.setPayload(message.getPayload(String.class).getBytes(encoding));
        } else if (message.getPayload() instanceof Resource) {
            try {
                message.setPayload(FileCopyUtils.copyToByteArray(message.getPayload(Resource.class).getInputStream()));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to build binary message payload from payload resource", e);
            }
        } else {
            message.setPayload(message.getPayload(byte[].class));
        }

        message.setType(MessageType.BINARY.name());
        return message;
    }

    /**
     * Specifies the encoding.
     * @param encoding
     */
    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }
}
