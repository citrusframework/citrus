/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.selenium.config.xml;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.selenium.actions.JavaScriptAction;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class JavaScriptActionParser extends AbstractBrowserActionParser {

    @Override
    protected void parseAction(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
        Element scriptElement = DomUtils.getChildElementByTagName(element, "script");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, DomUtils.getTextValue(scriptElement), "script");

        List<String> errors = new ArrayList<>();
        Element errorsElement = DomUtils.getChildElementByTagName(element, "errors");
        if (errorsElement != null) {
            List<Element> errorElements = DomUtils.getChildElementsByTagName(errorsElement, "error");
            if (!CollectionUtils.isEmpty(errorElements)) {
                for (Element error : errorElements) {
                    errors.add(DomUtils.getTextValue(error));
                }
            }
        }

        beanDefinition.addPropertyValue("expectedErrors", errors);
    }

    @Override
    protected Class<JavaScriptActionFactoryBean> getBrowserActionClass() {
        return JavaScriptActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static final class JavaScriptActionFactoryBean extends AbstractSeleniumActionFactoryBean<JavaScriptAction, JavaScriptAction.Builder> {

        private final JavaScriptAction.Builder builder = new JavaScriptAction.Builder();

        /**
         * Sets the script.
         * @param script
         */
        public void setScript(String script) {
            builder.script(script);
        }

        /**
         * Sets the arguments.
         * @param arguments
         */
        public void setArguments(List<Object> arguments) {
            arguments.forEach(builder::argument);
        }

        /**
         * Sets the expectedErrors.
         * @param expectedErrors
         */
        public void setExpectedErrors(List<String> expectedErrors) {
            builder.errors(expectedErrors);
        }

        @Override
        public JavaScriptAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return JavaScriptAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public JavaScriptAction.Builder getBuilder() {
            return builder;
        }
    }
}
