package org.citrusframework.citrus.integration.common;

import org.citrusframework.citrus.DefaultTestCase;
import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.TestCaseMetaInfo;
import org.citrusframework.citrus.annotations.CitrusXmlTest;
import org.citrusframework.citrus.common.BeanDefinitionParserConfiguration;
import org.citrusframework.citrus.common.SpringXmlTestLoaderConfiguration;
import org.citrusframework.citrus.config.xml.BaseTestCaseMetaInfoParser;
import org.citrusframework.citrus.config.xml.BaseTestCaseParser;
import org.citrusframework.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.xml.DomUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/**
 * @author Thorsten Schlathoelter
 */
@SpringXmlTestLoaderConfiguration(
        parserConfigurations = {
                @BeanDefinitionParserConfiguration(name = "testcase", parser = SpringXmlTestLoaderIT.CustomTestCaseParser.class),
                @BeanDefinitionParserConfiguration(name = "meta-info", parser = SpringXmlTestLoaderIT.CustomTestCaseMetaInfoParser.class)
        })
public class SpringXmlTestLoaderIT extends TestNGCitrusSpringSupport {

    @Test
    @CitrusXmlTest
    public void SpringXmlTestLoaderIT() {
        TestCase testCase = getTestCase();
        Assert.assertTrue(testCase instanceof CustomTestCase);

        TestCaseMetaInfo metaInfo = testCase.getMetaInfo();
        Assert.assertTrue(metaInfo instanceof CustomTestCaseMetaInfo);
        Assert.assertEquals(((CustomTestCaseMetaInfo)metaInfo).getDescription(), "Foo bar: F#!$§ed up beyond all repair");
    }

    /**
     * A custom test case implementation
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


}
