package com.consol.citrus.validation.interceptor;

import java.io.IOException;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class BinaryMessageConstructionInterceptorTest extends AbstractTestNGUnitTest {

    private BinaryMessageConstructionInterceptor interceptor = new BinaryMessageConstructionInterceptor();

    @Test
    public void testInterceptMessage() throws IOException {
        Assert.assertEquals(interceptor.interceptMessageConstruction(new DefaultMessage(), MessageType.PLAINTEXT.name(), context).getPayload(), "");
        Assert.assertEquals(interceptor.interceptMessageConstruction(new DefaultMessage(), MessageType.BINARY.name(), context).getPayload(), new byte[]{});
        Assert.assertEquals(interceptor.interceptMessageConstruction(new DefaultMessage("foo"), MessageType.PLAINTEXT.name(), context).getPayload(), "foo");
        Assert.assertEquals(interceptor.interceptMessageConstruction(new DefaultMessage("foo"), MessageType.BINARY.name(), context).getPayload(), "foo".getBytes());
        Assert.assertEquals(interceptor.interceptMessageConstruction(new DefaultMessage(getTestFile()), MessageType.PLAINTEXT.name(), context).getPayload(), getTestFile());
        Assert.assertEquals(interceptor.interceptMessageConstruction(new DefaultMessage(getTestFile()), MessageType.BINARY.name(), context).getPayload(), FileCopyUtils.copyToByteArray(getTestFile().getInputStream()));
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testInterceptMessageResourceNotFound() {
        interceptor.interceptMessageConstruction(new DefaultMessage(new FileSystemResource("foo.txt")), MessageType.BINARY.name(), context);
    }

    private Resource getTestFile() {
        return new ClassPathResource("foo.txt", BinaryMessageConstructionInterceptor.class);
    }
}