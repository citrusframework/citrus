package com.consol.citrus.validation;

import java.util.Map;

import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.json.JsonPathMessageValidator;
import com.consol.citrus.validation.json.JsonTextMessageValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class MessageValidatorTest {

    @Test
    public void testLookup() {
        Map<String, MessageValidator<? extends ValidationContext>> validators = MessageValidator.lookup();
        Assert.assertEquals(validators.size(), 3L);
        Assert.assertNotNull(validators.get("defaultMessageHeaderValidator"));
        Assert.assertEquals(validators.get("defaultMessageHeaderValidator").getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertNotNull(validators.get("defaultJsonMessageValidator"));
        Assert.assertEquals(validators.get("defaultJsonMessageValidator").getClass(), JsonTextMessageValidator.class);
        Assert.assertNotNull(validators.get("defaultJsonPathMessageValidator"));
        Assert.assertEquals(validators.get("defaultJsonPathMessageValidator").getClass(), JsonPathMessageValidator.class);
    }

    @Test
    public void testTestLookup() {
        Assert.assertTrue(MessageValidator.lookup("header").isPresent());
        Assert.assertTrue(MessageValidator.lookup("json").isPresent());
        Assert.assertTrue(MessageValidator.lookup("json-path").isPresent());
    }
}
