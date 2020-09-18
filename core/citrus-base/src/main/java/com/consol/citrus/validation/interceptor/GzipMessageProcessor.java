package com.consol.citrus.validation.interceptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

import com.consol.citrus.CitrusSettings;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.AbstractMessageProcessor;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;

/**
 * Message processor automatically converts message payloads to gzipped content. Supports String typed message payloads and
 * payload resources.
 *
 * @author Christoph Deppisch
 */
public class GzipMessageProcessor extends AbstractMessageProcessor {

    private Charset encoding = Charset.forName(CitrusSettings.CITRUS_FILE_ENCODING);

    @Override
    protected void processMessage(Message message, TestContext context) {
        if (message.getPayload() instanceof GZIPOutputStream) {
            return;
        }

        try {
            if (message.getPayload() instanceof String) {
                message.setPayload(getZipped(context.replaceDynamicContentInString(message.getPayload(String.class)).getBytes(encoding)));
            } else if (message.getPayload() instanceof Resource) {
                message.setPayload(getZipped(FileCopyUtils.copyToByteArray(message.getPayload(Resource.class).getInputStream())));
            } else if (message.getPayload() instanceof InputStream) {
                message.setPayload(getZipped(FileCopyUtils.copyToByteArray(message.getPayload(InputStream.class))));
            } else {
                message.setPayload(getZipped(message.getPayload(byte[].class)));
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to gzip message payload", e);
        }

        message.setType(MessageType.GZIP.name());
    }

    private byte[] getZipped(byte[] in) throws IOException {
        try (ByteArrayOutputStream zipped = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipped)) {
                StreamUtils.copy(in, gzipOutputStream);
            }
            return zipped.toByteArray();
        }
    }

    /**
     * Specifies the encoding.
     * @param encoding
     */
    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }
}
