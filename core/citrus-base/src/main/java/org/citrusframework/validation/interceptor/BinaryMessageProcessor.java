package org.citrusframework.validation.interceptor;

import java.nio.charset.Charset;

import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.AbstractMessageProcessor;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;

/**
 * Message construction processor automatically converts message payloads to binary content. Supports String typed message payloads and
 * payload resources.
 *
 * @author Christoph Deppisch
 */
public class BinaryMessageProcessor extends AbstractMessageProcessor {

    private final Charset encoding;

    public BinaryMessageProcessor() {
        this(Builder.toBinary());
    }

    public BinaryMessageProcessor(Builder builder) {
        this.encoding = builder.encoding;
    }

    @Override
    protected void processMessage(Message message, TestContext context) {
        if (message.getPayload() instanceof String) {
            message.setPayload(message.getPayload(String.class).getBytes(encoding));
        } else if (message.getPayload() instanceof Resource) {
            message.setPayload(FileUtils.copyToByteArray(message.getPayload(Resource.class)));
        } else {
            message.setPayload(message.getPayload(byte[].class));
        }

        message.setType(MessageType.BINARY.name());
    }

    /**
     * Fluent builder.
     */
    public static final class Builder implements MessageProcessor.Builder<BinaryMessageProcessor, Builder> {

        private Charset encoding = Charset.forName(CitrusSettings.CITRUS_FILE_ENCODING);

        public static Builder toBinary() {
            return new Builder();
        }

        /**
         * With custom charset encoding identified by its name.
         * @param charsetName
         * @return
         */
        public Builder encoding(String charsetName) {
            return encoding(Charset.forName(charsetName));
        }

        /**
         * With custom charset encoding.
         * @param encoding
         * @return
         */
        public Builder encoding(Charset encoding) {
            this.encoding = encoding;
            return this;
        }

        @Override
        public BinaryMessageProcessor build() {
            return new BinaryMessageProcessor(this);
        }
    }
}
