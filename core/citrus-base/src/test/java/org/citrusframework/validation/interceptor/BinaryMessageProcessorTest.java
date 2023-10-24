package org.citrusframework.validation.interceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class BinaryMessageProcessorTest extends UnitTestSupport {

    private final BinaryMessageProcessor processor = new BinaryMessageProcessor();

    @Test
    public void testBinaryMessageStaysUntouched(){

        //GIVEN
        final DefaultMessage message = new DefaultMessage("foo".getBytes(StandardCharsets.UTF_8));
        message.setType(MessageType.BINARY);

        //WHEN
        processor.process(message, context);

        //THEN
        assertEquals(message.getPayload(), "foo".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testTextMessageIsIntercepted(){

        //GIVEN
        final DefaultMessage message = new DefaultMessage("foo");
        message.setType(MessageType.PLAINTEXT);

        //WHEN
        processor.process(message, context);

        //THEN
        assertEquals(message.getPayload(), "foo".getBytes(StandardCharsets.UTF_8));
        assertEquals(message.getType(), MessageType.BINARY.name());
    }

    @Test
    public void testResourceMessageWithIsIntercepted() throws IOException {

        //GIVEN
        final DefaultMessage message = new DefaultMessage(getTestFile());
        message.setType(MessageType.PLAINTEXT);

        //WHEN
        processor.process(message, context);

        //THEN
        assertEquals(message.getPayload(), FileUtils.copyToByteArray(getTestFile().getInputStream()));
        assertEquals(message.getType(), MessageType.BINARY.name());
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testMessageResourceNotFound() {

        //GIVEN
        final DefaultMessage message = new DefaultMessage(Resources.fromFileSystem("unknown.txt"));
        message.setType(MessageType.PLAINTEXT);

        //WHEN
        processor.process(message, context);

        //THEN should throw exception
    }

    private Resource getTestFile() {
        return Resources.create("foo.txt", BinaryMessageProcessor.class);
    }
}
