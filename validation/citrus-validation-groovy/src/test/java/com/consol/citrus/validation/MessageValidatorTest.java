package com.consol.citrus.validation;

import java.util.Map;

import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.script.GroovyJsonMessageValidator;
import com.consol.citrus.validation.script.GroovyScriptMessageValidator;
import com.consol.citrus.validation.script.GroovyXmlMessageValidator;
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
        Assert.assertNotNull(validators.get("defaultGroovyTextMessageValidator"));
        Assert.assertEquals(validators.get("defaultGroovyTextMessageValidator").getClass(), GroovyScriptMessageValidator.class);
        Assert.assertNotNull(validators.get("defaultGroovyJsonMessageValidator"));
        Assert.assertEquals(validators.get("defaultGroovyJsonMessageValidator").getClass(), GroovyJsonMessageValidator.class);
        Assert.assertNotNull(validators.get("defaultGroovyXmlMessageValidator"));
        Assert.assertEquals(validators.get("defaultGroovyXmlMessageValidator").getClass(), GroovyXmlMessageValidator.class);
    }

    @Test
    public void testTestLookup() {
        Assert.assertTrue(MessageValidator.lookup("header").isPresent());
        Assert.assertTrue(MessageValidator.lookup("groovy-text").isPresent());
        Assert.assertTrue(MessageValidator.lookup("groovy-json").isPresent());
        Assert.assertTrue(MessageValidator.lookup("groovy-xml").isPresent());
    }
}
