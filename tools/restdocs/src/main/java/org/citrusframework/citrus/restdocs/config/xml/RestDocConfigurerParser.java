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

package org.citrusframework.citrus.restdocs.config.xml;

import org.citrusframework.citrus.restdocs.http.CitrusRestDocConfigurer;
import org.citrusframework.citrus.restdocs.soap.CitrusRestDocSoapConfigurer;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.restdocs.ManualRestDocumentation;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class RestDocConfigurerParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder;

        String docType = element.getAttribute("type");
        if (StringUtils.hasText(docType) && docType.equals("soap")) {
            builder = BeanDefinitionBuilder.rootBeanDefinition(CitrusRestDocSoapConfigurer.class);
        } else {
            builder = BeanDefinitionBuilder.rootBeanDefinition(CitrusRestDocConfigurer.class);
        }

        ManualRestDocumentation restDocumentation = new ManualRestDocumentation(element.getAttribute("output-directory"));
        builder.addConstructorArgValue(restDocumentation);

        return builder.getBeanDefinition();
    }
}
