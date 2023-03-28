package org.citrusframework.validation;

import java.util.Map;

import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.text.BinaryBase64MessageValidator;
import org.citrusframework.validation.text.GzipBinaryBase64MessageValidator;
import org.citrusframework.validation.text.PlainTextMessageValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class MessageValidatorTest {

    @Test
    public void testLookup() {
        Map<String, MessageValidator<? extends ValidationContext>> validators = MessageValidator.lookup();
        Assert.assertEquals(validators.size(), 4L);
        Assert.assertNotNull(validators.get("defaultMessageHeaderValidator"));
        Assert.assertEquals(validators.get("defaultMessageHeaderValidator").getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertNotNull(validators.get("defaultBinaryBase64MessageValidator"));
        Assert.assertEquals(validators.get("defaultBinaryBase64MessageValidator").getClass(), BinaryBase64MessageValidator.class);
        Assert.assertNotNull(validators.get("defaultGzipBinaryBase64MessageValidator"));
        Assert.assertEquals(validators.get("defaultGzipBinaryBase64MessageValidator").getClass(), GzipBinaryBase64MessageValidator.class);
        Assert.assertNotNull(validators.get("defaultPlaintextMessageValidator"));
        Assert.assertEquals(validators.get("defaultPlaintextMessageValidator").getClass(), PlainTextMessageValidator.class);
    }

    @Test
    public void testTestLookup() {
        Assert.assertTrue(MessageValidator.lookup("header").isPresent());
        Assert.assertTrue(MessageValidator.lookup("binary_base64").isPresent());
        Assert.assertTrue(MessageValidator.lookup("gzip_base64").isPresent());
        Assert.assertTrue(MessageValidator.lookup("plaintext").isPresent());
    }
}
