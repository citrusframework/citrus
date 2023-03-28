/*
 *  Copyright 2006-2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.config.xml;

import org.citrusframework.DefaultTestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.config.CitrusNamespaceParserRegistry;
import org.citrusframework.testng.AbstractActionParserTest;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.xml.DomUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import java.io.*;

/**
 * @author Thorsten Schlatoelter
 */
public class CustomTestCaseParserTest extends AbstractActionParserTest<EchoAction> {

    @BeforeClass
    public void parseBeanDefinitions() {
        CitrusNamespaceParserRegistry.registerParser("testcase", new CustomTestCaseParser());
        CitrusNamespaceParserRegistry.registerParser("meta-info", new CustomTestCaseMetaInfoParser());
        super.parseBeanDefinitions();
    }

    @AfterClass
    public static void cleanup() {
        CitrusNamespaceParserRegistry.registerParser("testcase", new TestCaseParser());
        CitrusNamespaceParserRegistry.registerParser("meta-info", new TestCaseMetaInfoParser());
    }

    @Test
    public void testCustomTestCaseParser() throws IOException {
        Assert.assertTrue(getTestCase() instanceof CustomTestCase);
        Assert.assertTrue(getTestCase().getMetaInfo() instanceof CustomTestCaseMetaInfo);
        Assert.assertEquals(((CustomTestCaseMetaInfo)getTestCase().getMetaInfo()).getDescription(), "Foo bar: F#!$§ed up beyond all repair");
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

    public static class CustomTestCaseParser extends BaseTestCaseParser {

        public CustomTestCaseParser() {
            super(CustomTestCase.class);
        }

    }
}
