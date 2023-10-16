package org.citrusframework.validation.interceptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.AbstractMessageProcessor;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;

/**
 * Message processor automatically converts message payloads to gzipped content. Supports String typed message payloads and
 * payload resources.
 *
 * @author Christoph Deppisch
 */
public class GzipMessageProcessor extends AbstractMessageProcessor {

    private final Charset encoding;

    public GzipMessageProcessor() {
        this(Builder.toGzip());
    }

    public GzipMessageProcessor(Builder builder) {
        this.encoding = builder.encoding;
    }

    @Override
    protected void processMessage(Message message, TestContext context) {
        if (message.getPayload() instanceof GZIPOutputStream) {
            return;
        }

        try {
            if (message.getPayload() instanceof String) {
                message.setPayload(getZipped(context.replaceDynamicContentInString(message.getPayload(String.class)).getBytes(encoding)));
            } else if (message.getPayload() instanceof Resource) {
                message.setPayload(getZipped(FileUtils.copyToByteArray(message.getPayload(Resource.class))));
            } else if (message.getPayload() instanceof InputStream) {
                message.setPayload(getZipped(FileUtils.copyToByteArray(message.getPayload(InputStream.class))));
            } else {
                message.setPayload(getZipped(message.getPayload(byte[].class)));
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to gzip message payload", e);
        }

        message.setType(MessageType.GZIP.name());
    }

    /**
     * Fluent builder.
     */
    public static final class Builder implements MessageProcessor.Builder<GzipMessageProcessor, Builder> {

        private Charset encoding = Charset.forName(CitrusSettings.CITRUS_FILE_ENCODING);

        public static Builder toGzip() {
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
        public GzipMessageProcessor build() {
            return new GzipMessageProcessor(this);
        }
    }

    private byte[] getZipped(byte[] in) throws IOException {
        try (ByteArrayOutputStream zipped = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipped)) {
                gzipOutputStream.write(in);
                gzipOutputStream.flush();
            }
            return zipped.toByteArray();
        }
    }
}
