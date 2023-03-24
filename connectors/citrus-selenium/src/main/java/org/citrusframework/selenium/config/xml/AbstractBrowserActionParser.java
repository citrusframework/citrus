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

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractTestActionFactoryBean;
import org.citrusframework.config.xml.DescriptionElementParser;
import org.citrusframework.selenium.actions.AbstractSeleniumAction;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for selenium client action in test case.
 *
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public abstract class AbstractBrowserActionParser implements BeanDefinitionParser {

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(getBrowserActionClass());

        DescriptionElementParser.doParse(element, beanDefinition);
        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("browser"), "browser");

        parseAction(beanDefinition, element, parserContext);

        return beanDefinition.getBeanDefinition();
    }

    protected abstract void parseAction(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext);

    protected abstract Class<? extends AbstractSeleniumActionFactoryBean<?, ?>> getBrowserActionClass();

    /**
     * Test action factory bean.
     */
    public static abstract class AbstractSeleniumActionFactoryBean<T extends AbstractSeleniumAction, B extends AbstractSeleniumAction.Builder<?, ?>> extends AbstractTestActionFactoryBean<T, B> {

        /**
         * Sets the Selenium browser.
         * @param browser
         */
        public void setBrowser(SeleniumBrowser browser) {
            getBuilder().browser(browser);
        }
    }
}
