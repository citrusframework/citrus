package com.consol.citrus.junit.integration;

import com.consol.citrus.*;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.common.TestCaseParserConfiguration;
import com.consol.citrus.common.TestLoader;
import com.consol.citrus.common.XmlTestLoader;
import com.consol.citrus.common.XmlTestLoaderConfiguration;
import com.consol.citrus.config.xml.BaseTestCaseMetaInfoParser;
import com.consol.citrus.config.xml.BaseTestCaseParser;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.junit.spring.JUnit4CitrusSpringSupport;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Thorsten Schlathoelter
 */
@XmlTestLoaderConfiguration(
        parserConfigurations = {@TestCaseParserConfiguration(name = "testcase", parser = XMlTestLoaderIT.CustomTestCaseParser.class),
                @TestCaseParserConfiguration(name = "meta-info", parser = XMlTestLoaderIT.CustomTestCaseMetaInfoParser.class)}
)
public class XMlTestLoaderIT extends JUnit4CitrusSpringSupport {

    private static TestCase loadedTestCase;

    @Test
    @CitrusXmlTest
    public void XmlTestLoaderIT() {
        // Special validation is performed via ValidateTestCaseAndMetaInfoAction.
    }

    /**
     * Hack to get a hold on the loadedTestCase. As the test is executed as CitrusXmlTest, it is not possible to add validation in the test method itself
     * @param testName
     * @param packageName
     * @return
     */
    protected TestLoader createTestLoader(String testName, String packageName) {
        return new XmlTestLoader(getClass(), testName, packageName, CitrusSpringContext.create(applicationContext)) {
            @Override
            public TestCase load() {
                loadedTestCase = super.load();
                return loadedTestCase;
            }
        };
    }

    /**
     * A custom test case implementation that should be created by the loader
     */
    public static class CustomTestCase extends DefaultTestCase {
    }

    /**
     * A custom test case meta info that should be created by the loader
     */
    public static class CustomTestCaseMetaInfo extends TestCaseMetaInfo {

        private String description;

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * A custom test case parser implementation used to create the custom test case
     */
    public static class CustomTestCaseParser extends BaseTestCaseParser<CustomTestCase> {

        public CustomTestCaseParser() {
            super(CustomTestCase.class);
        }
    }

    /**
     * A custom meta info parser used to create the custom test case meta info
     */
    public static class CustomTestCaseMetaInfoParser extends BaseTestCaseMetaInfoParser<CustomTestCaseMetaInfo> {

        public CustomTestCaseMetaInfoParser() {
            super(CustomTestCaseMetaInfo.class);
        }

        @Override
        protected void parseAdditionalProperties(Element metaInfoElement, BeanDefinitionBuilder metaInfoBuilder) {
            Element descriptionElement = DomUtils.getChildElementByTagName(metaInfoElement, "description");
            if (descriptionElement != null) {
                String description = DomUtils.getTextValue(descriptionElement);
                metaInfoBuilder.addPropertyValue("description", description);
            }
        }
    }



    /**
     * A validation action that asserts, that the correct types of TestCase and TestCaseMetaInfo
     */
    public static class ValidateTestCaseAndMetaInfoAction extends AbstractTestAction {

        @Override
        public void doExecute(TestContext context) {
            Assert.assertTrue(loadedTestCase instanceof CustomTestCase);
            TestCaseMetaInfo metaInfo = loadedTestCase.getMetaInfo();
            Assert.assertTrue(metaInfo instanceof CustomTestCaseMetaInfo);
            Assert.assertEquals(((CustomTestCaseMetaInfo)metaInfo).getDescription(), "Foo bar: F#!$§ed up beyond all repair");
        }
    }
}
