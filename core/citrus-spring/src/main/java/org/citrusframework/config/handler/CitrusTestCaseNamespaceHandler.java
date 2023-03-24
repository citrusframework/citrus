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

package org.citrusframework.config.handler;

import java.util.Map;

import org.citrusframework.config.CitrusNamespaceParserRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Namespace handler registers bean definition parser
 * for Citrus testcase schema elements.
 *
 * @author Christoph Deppisch, Thorsten Schlathoelter
 * @since 2007
 */
public class CitrusTestCaseNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {

        for (Map.Entry<String, BeanDefinitionParser> actionParserEntry : CitrusNamespaceParserRegistry.getRegisteredBeanParser().entrySet()) {
            registerBeanDefinitionParser(actionParserEntry.getKey(), actionParserEntry.getValue());
        }

        for (Map.Entry<String, BeanDefinitionParser> actionParserEntry : CitrusNamespaceParserRegistry.lookupBeanParser().entrySet()) {
            registerBeanDefinitionParser(actionParserEntry.getKey(), actionParserEntry.getValue());
        }
    }

}
