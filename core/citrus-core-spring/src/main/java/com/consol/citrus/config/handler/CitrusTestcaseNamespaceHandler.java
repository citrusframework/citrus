/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.config.handler;

import com.consol.citrus.config.TestActionRegistry;
import com.consol.citrus.config.xml.*;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import java.util.Map;

/**
 * Namespace handler registers bean definition parser
 * for Citrus testcase schema elements.
 *
 * @author Christoph Deppisch
 * @since 2007
 */
public class CitrusTestcaseNamespaceHandler extends NamespaceHandlerSupport {

    /**
     * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
     */
    public void init() {
        registerBeanDefinitionParser("testcase", new TestCaseParser());
        registerBeanDefinitionParser("template", new TemplateParser());

        for (Map.Entry<String, BeanDefinitionParser> actionParserEntry : TestActionRegistry.getRegisteredActionParser().entrySet()) {
            registerBeanDefinitionParser(actionParserEntry.getKey(), actionParserEntry.getValue());
        }
    }
}
