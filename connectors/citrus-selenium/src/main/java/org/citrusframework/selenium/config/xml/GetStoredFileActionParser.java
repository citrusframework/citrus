/*
 * Copyright 2006-2017 the original author or authors.
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
import org.citrusframework.selenium.actions.*;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class GetStoredFileActionParser extends AbstractBrowserActionParser {

    @Override
    protected void parseAction(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("file-name"), "fileName");
    }

    @Override
    protected Class<GetStoredFileActionFactoryBean> getBrowserActionClass() {
        return GetStoredFileActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static final class GetStoredFileActionFactoryBean extends AbstractSeleniumActionFactoryBean<GetStoredFileAction, GetStoredFileAction.Builder> {

        private final GetStoredFileAction.Builder builder = new GetStoredFileAction.Builder();

        /**
         * Sets the fileName.
         * @param fileName
         */
        public void setFileName(String fileName) {
            builder.fileName(fileName);
        }

        @Override
        public GetStoredFileAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return GetStoredFileAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public GetStoredFileAction.Builder getBuilder() {
            return builder;
        }
    }
}
