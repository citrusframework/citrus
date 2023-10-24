package org.citrusframework.junit.integration;

import org.citrusframework.DefaultTestCase;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.common.BeanDefinitionParserConfiguration;
import org.citrusframework.common.SpringXmlTestLoaderConfiguration;
import org.citrusframework.config.xml.BaseTestCaseMetaInfoParser;
import org.citrusframework.config.xml.BaseTestCaseParser;
import org.citrusframework.junit.spring.JUnit4CitrusSpringSupport;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Thorsten Schlathoelter
 */
@SpringXmlTestLoaderConfiguration(
        parserConfigurations = {
                @BeanDefinitionParserConfiguration(name = "testcase", parser = SpringXmlTestLoaderIT.CustomTestCaseParser.class),
                @BeanDefinitionParserConfiguration(name = "meta-info", parser = SpringXmlTestLoaderIT.CustomTestCaseMetaInfoParser.class)
        })
public class SpringXmlTestLoaderIT extends JUnit4CitrusSpringSupport {

    @Test
    @CitrusTestSource(type = TestLoader.SPRING)
    public void SpringXmlTestLoaderIT() {
        TestCase testCase = getTestCase();
        Assert.assertTrue(testCase instanceof CustomTestCase);

        TestCaseMetaInfo metaInfo = testCase.getMetaInfo();
        Assert.assertTrue(metaInfo instanceof CustomTestCaseMetaInfo);
        Assert.assertEquals(((CustomTestCaseMetaInfo)metaInfo).getDescription(), "Foo bar: F#!$§ed up beyond all repair");
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
}
