package com.consol.citrus.validation.interceptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class GzipMessageConstructionInterceptorTest extends AbstractTestNGUnitTest {

    private GzipMessageConstructionInterceptor interceptor = new GzipMessageConstructionInterceptor();

    @Test
    public void testInterceptMessage() throws IOException {
        Assert.assertEquals(interceptor.interceptMessageConstruction(new DefaultMessage(), MessageType.PLAINTEXT.name(), context).getPayload(), "");

        ByteArrayOutputStream unzipped = new ByteArrayOutputStream();
        GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(interceptor.interceptMessageConstruction(new DefaultMessage(), MessageType.GZIP.name(), context).getPayload(byte[].class)));
        StreamUtils.copy(gzipInputStream, unzipped);
        Assert.assertEquals(unzipped.toByteArray(), new byte[]{});

        Assert.assertEquals(interceptor.interceptMessageConstruction(new DefaultMessage("foo"), MessageType.PLAINTEXT.name(), context).getPayload(), "foo");

        unzipped = new ByteArrayOutputStream();
        gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(interceptor.interceptMessageConstruction(new DefaultMessage("foo"), MessageType.GZIP.name(), context).getPayload(byte[].class)));
        StreamUtils.copy(gzipInputStream, unzipped);
        Assert.assertEquals(unzipped.toByteArray(), "foo".getBytes());

        Assert.assertEquals(interceptor.interceptMessageConstruction(new DefaultMessage(getTestFile()), MessageType.PLAINTEXT.name(), context).getPayload(), getTestFile());

        unzipped = new ByteArrayOutputStream();
        gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(interceptor.interceptMessageConstruction(new DefaultMessage(getTestFile()), MessageType.GZIP.name(), context).getPayload(byte[].class)));
        StreamUtils.copy(gzipInputStream, unzipped);
        Assert.assertEquals(unzipped.toByteArray(), FileCopyUtils.copyToByteArray(getTestFile().getInputStream()));
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testInterceptMessageResourceNotFound() {
        interceptor.interceptMessageConstruction(new DefaultMessage(new FileSystemResource("foo.txt")), MessageType.GZIP.name(), context);
    }

    private Resource getTestFile() {
        return new ClassPathResource("foo.txt", GzipMessageConstructionInterceptor.class);
    }
}