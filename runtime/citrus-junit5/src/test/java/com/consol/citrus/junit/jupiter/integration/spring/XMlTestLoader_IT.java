package com.consol.citrus.junit.jupiter.integration.spring;

import com.consol.citrus.DefaultTestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.common.TestCaseParserConfiguration;
import com.consol.citrus.common.XmlTestLoaderConfiguration;
import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.config.xml.BaseTestCaseMetaInfoParser;
import com.consol.citrus.config.xml.BaseTestCaseParser;
import com.consol.citrus.junit.jupiter.spring.CitrusSpringSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Thorsten Schlathoelter
 */
@CitrusSpringSupport
@ContextConfiguration(classes = {CitrusSpringConfig.class})
@XmlTestLoaderConfiguration(
        parserConfigurations = {@TestCaseParserConfiguration(name = "testcase", parser = XMlTestLoader_IT.CustomTestCaseParser.class),
                @TestCaseParserConfiguration(name = "meta-info", parser = XMlTestLoader_IT.CustomTestCaseMetaInfoParser.class)})
public class XMlTestLoader_IT {

    private static CustomTestCase testCase;

    @Test
    @CitrusXmlTest(name="XmlTestLoader_IT")
    public void XmlTestLoaderIT_0_IT(@CitrusResource TestCaseRunner runner) {
        Assertions.assertNotNull(testCase);

        TestCaseMetaInfo metaInfo = testCase.getMetaInfo();
        Assertions.assertTrue(metaInfo instanceof CustomTestCaseMetaInfo);
        Assertions.assertEquals(((CustomTestCaseMetaInfo)metaInfo).getDescription(), "Foo bar: F#!$§ed up beyond all repair");
    }

    /**
     * A custom test case implementation that should be created by the loader
     */
    public static class CustomTestCase extends DefaultTestCase {
        public CustomTestCase() {
            testCase = this;
        }
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
            super.parseAdditionalProperties(metaInfoElement, metaInfoBuilder);
            Element descriptionElement = DomUtils.getChildElementByTagName(metaInfoElement, "description");
            if (descriptionElement != null) {
                String description = DomUtils.getTextValue(descriptionElement);
                metaInfoBuilder.addPropertyValue("description", description);
            }
        }

    }


}
