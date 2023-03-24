package org.citrusframework.junit.jupiter.integration.spring;

import org.citrusframework.DefaultTestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusXmlTest;
import org.citrusframework.common.BeanDefinitionParserConfiguration;
import org.citrusframework.common.SpringXmlTestLoaderConfiguration;
import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.config.xml.BaseTestCaseMetaInfoParser;
import org.citrusframework.config.xml.BaseTestCaseParser;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
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
@SpringXmlTestLoaderConfiguration(
        parserConfigurations = {
                @BeanDefinitionParserConfiguration(name = "testcase", parser = SpringXmlTestLoader_IT.CustomTestCaseParser.class),
                @BeanDefinitionParserConfiguration(name = "meta-info", parser = SpringXmlTestLoader_IT.CustomTestCaseMetaInfoParser.class)
        })
public class SpringXmlTestLoader_IT {

    @Test
    @CitrusXmlTest(name="SpringXmlTestLoader_IT")
    public void SpringXmlTestLoaderIT_0_IT(@CitrusResource TestCaseRunner runner) {
        Assertions.assertNotNull(runner.getTestCase());

        TestCaseMetaInfo metaInfo = runner.getTestCase().getMetaInfo();
        Assertions.assertTrue(metaInfo instanceof CustomTestCaseMetaInfo);
        Assertions.assertEquals(((CustomTestCaseMetaInfo)metaInfo).getDescription(), "Foo bar: F#!$§ed up beyond all repair");
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
            super.parseAdditionalProperties(metaInfoElement, metaInfoBuilder);
            Element descriptionElement = DomUtils.getChildElementByTagName(metaInfoElement, "description");
            if (descriptionElement != null) {
                String description = DomUtils.getTextValue(descriptionElement);
                metaInfoBuilder.addPropertyValue("description", description);
            }
        }

    }


}
