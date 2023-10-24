package org.citrusframework.validation.interceptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Christoph Deppisch
 */
public class GzipMessageProcessorTest extends UnitTestSupport {

    private final GzipMessageProcessor processor = new GzipMessageProcessor();

    @Test
    public void testGzipMessageStaysUntouched() throws IOException {
        try (ByteArrayOutputStream zipped = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipped)) {
                gzipOutputStream.write("foo".getBytes(StandardCharsets.UTF_8));

                //GIVEN
                final DefaultMessage message = new DefaultMessage(gzipOutputStream);
                message.setType(MessageType.GZIP);

                //WHEN
                processor.process(message, context);

                //THEN
                assertEquals(message.getPayload(), gzipOutputStream);
            }
        }
    }

    @Test
    public void testTextMessageIsIntercepted() throws IOException {

        //GIVEN
        final DefaultMessage message = new DefaultMessage("foo");
        message.setType(MessageType.PLAINTEXT);

        //WHEN
        processor.process(message, context);

        //THEN
        assertEquals(message.getType(), MessageType.GZIP.name());
        try (ByteArrayOutputStream unzipped = new ByteArrayOutputStream();
             GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(message.getPayload(byte[].class)));) {
            unzipped.write(gzipInputStream.readAllBytes());
            Assert.assertEquals(unzipped.toByteArray(), "foo".getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testBinaryMessageIsIntercepted() throws IOException {

        //GIVEN
        final DefaultMessage message = new DefaultMessage("foo".getBytes(StandardCharsets.UTF_8));
        message.setType(MessageType.BINARY);

        //WHEN
        processor.process(message, context);

        //THEN
        assertEquals(message.getType(), MessageType.GZIP.name());
        try (ByteArrayOutputStream unzipped = new ByteArrayOutputStream();
             GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(message.getPayload(byte[].class)));) {
            unzipped.write(gzipInputStream.readAllBytes());
            Assert.assertEquals(unzipped.toByteArray(), "foo".getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testInputStreamMessageIsIntercepted() throws IOException {

        //GIVEN
        final DefaultMessage message = new DefaultMessage(new ByteArrayInputStream("foo".getBytes()));
        message.setType(MessageType.PLAINTEXT);

        //WHEN
        processor.process(message, context);

        //THEN
        assertEquals(message.getType(), MessageType.GZIP.name());
        try (ByteArrayOutputStream unzipped = new ByteArrayOutputStream();
             GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(message.getPayload(byte[].class)));) {
            unzipped.write(gzipInputStream.readAllBytes());
            Assert.assertEquals(unzipped.toByteArray(), "foo".getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testResourceMessageIsIntercepted() throws IOException {

        //GIVEN
        final DefaultMessage message = new DefaultMessage(getTestFile());
        message.setType(MessageType.PLAINTEXT);

        //WHEN
        processor.process(message, context);

        //THEN
        assertEquals(message.getType(), MessageType.GZIP.name());
        try (ByteArrayOutputStream unzipped = new ByteArrayOutputStream();
             GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(message.getPayload(byte[].class)));) {
            unzipped.write(gzipInputStream.readAllBytes());
            Assert.assertEquals(unzipped.toByteArray(), FileUtils.copyToByteArray(getTestFile().getInputStream()));
        }
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testProcessMessageResourceNotFound() {

        //GIVEN
        final DefaultMessage message = new DefaultMessage(Resources.fromFileSystem("unknown.txt"));
        message.setType(MessageType.PLAINTEXT);

        //WHEN
        processor.process(message, context);

        //THEN should throw exception
    }

    private Resource getTestFile() {
        return Resources.create("foo.txt", GzipMessageProcessor.class);
    }
}
