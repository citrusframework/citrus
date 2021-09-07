package com.consol.citrus.validation.interceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.consol.citrus.UnitTestSupport;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.MessageType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class BinaryMessageProcessorTest extends UnitTestSupport {

    private BinaryMessageProcessor processor = new BinaryMessageProcessor();

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
        assertEquals(message.getPayload(), FileCopyUtils.copyToByteArray(getTestFile().getInputStream()));
        assertEquals(message.getType(), MessageType.BINARY.name());
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testMessageResourceNotFound() {

        //GIVEN
        final DefaultMessage message = new DefaultMessage(new FileSystemResource("unknown.txt"));
        message.setType(MessageType.PLAINTEXT);

        //WHEN
        processor.process(message, context);

        //THEN should throw exception
    }

    private Resource getTestFile() {
        return new ClassPathResource("foo.txt", BinaryMessageProcessor.class);
    }
}
