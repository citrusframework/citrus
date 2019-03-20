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
import org.springframework.util.StreamUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import static org.testng.Assert.assertEquals;

/**
 * @author Christoph Deppisch
 */
public class GzipMessageConstructionInterceptorTest extends AbstractTestNGUnitTest {

    private GzipMessageConstructionInterceptor interceptor = new GzipMessageConstructionInterceptor();

    @Test
    public void testTextMessageWithTypesOtherThanGzipStaysUntouched(){

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
    public void testTextMessageWithTypeGzipIsIntercepted() throws IOException {

        //GIVEN
        final DefaultMessage message = new DefaultMessage("foo");
        MessageType messageType = MessageType.GZIP;


        //WHEN
        final Message interceptedMessage =
                interceptor.interceptMessageConstruction(message, messageType.name(), context);

        //THEN
        ByteArrayOutputStream unzipped = new ByteArrayOutputStream();
        GZIPInputStream gzipInputStream = new GZIPInputStream(
                new ByteArrayInputStream(interceptedMessage.getPayload(byte[].class)));
        StreamUtils.copy(gzipInputStream, unzipped);
        Assert.assertEquals(unzipped.toByteArray(), "foo".getBytes());
    }

    @Test
    public void testResourceMessageWithTypesOtherThanGzipStaysUntouched(){

        Assert.assertEquals(interceptor.interceptMessageConstruction(new DefaultMessage(getTestFile()), MessageType.PLAINTEXT.name(), context).getPayload(), getTestFile());

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
    public void testResourceMessageWithTypeGzipIsIntercepted() throws IOException {

        //GIVEN
        final DefaultMessage message = new DefaultMessage(getTestFile());
        MessageType messageType = MessageType.GZIP;

        //WHEN
        final Message interceptedMessage =
                interceptor.interceptMessageConstruction(message, messageType.name(), context);

        //THEN
        ByteArrayOutputStream unzipped = new ByteArrayOutputStream();
        GZIPInputStream gzipInputStream = new GZIPInputStream(
                new ByteArrayInputStream(interceptedMessage.getPayload(byte[].class)));
        StreamUtils.copy(gzipInputStream, unzipped);

        Assert.assertEquals(unzipped.toByteArray(),  FileCopyUtils.copyToByteArray(getTestFile().getInputStream()));
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testInterceptMessageResourceNotFound() {
        interceptor.interceptMessageConstruction(new DefaultMessage(new FileSystemResource("foo.txt")), MessageType.GZIP.name(), context);
    }

    private Resource getTestFile() {
        return new ClassPathResource("foo.txt", GzipMessageConstructionInterceptor.class);
    }
}