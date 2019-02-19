package com.consol.citrus.validation.interceptor;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

public class BinaryMessageConstructionInterceptorTest extends AbstractTestNGUnitTest {

    private BinaryMessageConstructionInterceptor interceptor = new BinaryMessageConstructionInterceptor();

    @Test
    public void testTextMessageWithTypesOtherThanBinaryStaysUntouched(){

        //GIVEN
        final DefaultMessage message = new DefaultMessage("foo");
        MessageType messageType = MessageType.PLAINTEXT;


        //WHEN
        final Message interceptedMessage =
                interceptor.interceptMessageConstruction(message, messageType.name(), context);

        //THEN
        assertEquals(interceptedMessage.getPayload(), "foo");
    }

    @Test
    public void testTextMessageWithTypeBinaryIsIntercepted(){

        //GIVEN
        final DefaultMessage message = new DefaultMessage("foo");
        MessageType messageType = MessageType.BINARY;


        //WHEN
        final Message interceptedMessage =
                interceptor.interceptMessageConstruction(message, messageType.name(), context);

        //THEN
        assertEquals(interceptedMessage.getPayload(), "foo".getBytes());
    }

    @Test
    public void testResourceMessageWithTypesOtherThanBinaryStaysUntouched(){

        //GIVEN
        final DefaultMessage message = new DefaultMessage(getTestFile());
        MessageType messageType = MessageType.PLAINTEXT;


        //WHEN
        final Message interceptedMessage =
                interceptor.interceptMessageConstruction(message, messageType.name(), context);

        //THEN
        assertEquals(interceptedMessage.getPayload(), getTestFile());
    }

    @Test
    public void testResourceMessageWithTypeBinaryIsIntercepted() throws IOException {

        //GIVEN
        final DefaultMessage message = new DefaultMessage(getTestFile());
        MessageType messageType = MessageType.BINARY;

        //WHEN
        final Message interceptedMessage =
                interceptor.interceptMessageConstruction(message, messageType.name(), context);

        //THEN
        assertEquals(interceptedMessage.getPayload(), FileCopyUtils.copyToByteArray(getTestFile().getInputStream()));
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testInterceptMessageResourceNotFound() {
        interceptor.interceptMessageConstruction(
                new DefaultMessage(new FileSystemResource("foo.txt")),
                MessageType.BINARY.name(), context);
    }

    private Resource getTestFile() {
        return new ClassPathResource("foo.txt", BinaryMessageConstructionInterceptor.class);
    }
}