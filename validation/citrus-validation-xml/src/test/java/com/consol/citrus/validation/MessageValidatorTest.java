package com.consol.citrus.validation;

import java.util.Map;

import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xhtml.XhtmlMessageValidator;
import com.consol.citrus.validation.xhtml.XhtmlXpathMessageValidator;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XpathMessageValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class MessageValidatorTest {

    @Test
    public void testLookup() {
        Map<String, MessageValidator<? extends ValidationContext>> validators = MessageValidator.lookup();
        Assert.assertEquals(validators.size(), 5L);
        Assert.assertNotNull(validators.get("defaultMessageHeaderValidator"));
        Assert.assertEquals(validators.get("defaultMessageHeaderValidator").getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertNotNull(validators.get("defaultXmlMessageValidator"));
        Assert.assertEquals(validators.get("defaultXmlMessageValidator").getClass(), DomXmlMessageValidator.class);
        Assert.assertNotNull(validators.get("defaultXpathMessageValidator"));
        Assert.assertEquals(validators.get("defaultXpathMessageValidator").getClass(), XpathMessageValidator.class);
        Assert.assertNotNull(validators.get("defaultXhtmlMessageValidator"));
        Assert.assertEquals(validators.get("defaultXhtmlMessageValidator").getClass(), XhtmlMessageValidator.class);
        Assert.assertNotNull(validators.get("defaultXhtmlXpathMessageValidator"));
        Assert.assertEquals(validators.get("defaultXhtmlXpathMessageValidator").getClass(), XhtmlXpathMessageValidator.class);
    }

    @Test
    public void testTestLookup() {
        Assert.assertTrue(MessageValidator.lookup("header").isPresent());
        Assert.assertTrue(MessageValidator.lookup("xml").isPresent());
        Assert.assertTrue(MessageValidator.lookup("xpath").isPresent());
        Assert.assertTrue(MessageValidator.lookup("xhtml").isPresent());
        Assert.assertTrue(MessageValidator.lookup("xhtml-xpath").isPresent());
    }
}
