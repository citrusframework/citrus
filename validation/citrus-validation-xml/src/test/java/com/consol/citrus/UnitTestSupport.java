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
        factory.getFunctionRegistry().getFunctionLibraries().add(new DefaultFunctionLibrary());
        factory.getValidationMatcherRegistry().getValidationMatcherLibraries().add(new DefaultValidationMatcherLibrary());

        factory.getMessageValidatorRegistry().getMessageValidators().put("header", new DefaultMessageHeaderValidator());
        factory.getMessageValidatorRegistry().getMessageValidators().put("xml", new DomXmlMessageValidator());
        factory.getMessageValidatorRegistry().getMessageValidators().put("xpath", new XpathMessageValidator());
        factory.getMessageValidatorRegistry().getMessageValidators().put("xhtml", new XhtmlMessageValidator());
        factory.getMessageValidatorRegistry().getMessageValidators().put("xhtmlXpath", new XhtmlXpathMessageValidator());

        return factory;
    }
}
