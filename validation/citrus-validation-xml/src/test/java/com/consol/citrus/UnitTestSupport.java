package com.consol.citrus;

import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.functions.DefaultFunctionLibrary;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.DefaultMessageHeaderValidator;
import com.consol.citrus.validation.matcher.DefaultValidationMatcherLibrary;
import com.consol.citrus.validation.xhtml.XhtmlMessageValidator;
import com.consol.citrus.validation.xhtml.XhtmlXpathMessageValidator;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XpathMessageValidator;

/**
 * @author Christoph Deppisch
 */
public abstract class UnitTestSupport extends AbstractTestNGUnitTest {

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getFunctionRegistry().addFunctionLibrary(new DefaultFunctionLibrary());
        factory.getValidationMatcherRegistry().addValidationMatcherLibrary(new DefaultValidationMatcherLibrary());

        factory.getMessageValidatorRegistry().addMessageValidator("header", new DefaultMessageHeaderValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("xml", new DomXmlMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("xpath", new XpathMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("xhtml", new XhtmlMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("xhtmlXpath", new XhtmlXpathMessageValidator());

        return factory;
    }
}
