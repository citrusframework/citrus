/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.spring.config.xml;

import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.spring.config.CitrusNamespaceParserRegistry;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class CustomTestCaseParserTest extends AbstractActionParserTest<EchoAction> {

    @BeforeAll
    public void parseBeanDefinitions() {
        CitrusNamespaceParserRegistry.registerParser("testcase", new CustomTestCaseParser());
        CitrusNamespaceParserRegistry.registerParser("meta-info", new CustomTestCaseMetaInfoParser());
        super.parseBeanDefinitions();
    }

    @AfterAll
    public static void cleanup() {
        CitrusNamespaceParserRegistry.registerParser("testcase", new TestCaseParser());
        CitrusNamespaceParserRegistry.registerParser("meta-info", new TestCaseMetaInfoParser());
    }

    @Test
    public void testCustomTestCaseParser() {
        Assertions.assertTrue(getTestCase() instanceof CustomTestCase);
        Assertions.assertTrue(getTestCase().getMetaInfo() instanceof CustomTestCaseMetaInfo);
        Assertions.assertEquals("Foo bar: F#!$§ed up beyond all repair", ((CustomTestCaseMetaInfo)getTestCase().getMetaInfo()).getDescription());
    }

    public static class CustomTestCase extends DefaultTestCase {
    }

    public static class CustomTestCaseMetaInfo extends TestCaseMetaInfo {
        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

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

    public static class CustomTestCaseParser extends BaseTestCaseParser<CustomTestCase> {

        public CustomTestCaseParser() {
            super(CustomTestCase.class);
        }

    }
}
