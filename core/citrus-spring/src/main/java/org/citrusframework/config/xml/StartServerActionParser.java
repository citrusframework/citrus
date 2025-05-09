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

package org.citrusframework.config.xml;

import java.util.List;

import org.citrusframework.actions.StartServerAction;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.server.Server;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @since 2.2
 */
public class StartServerActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(StartServerActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("server"), "server");

        ManagedList<RuntimeBeanReference> servers = new ManagedList<>();
        Element serversElement = DomUtils.getChildElementByTagName(element, "servers");
        if (serversElement != null) {
            List<Element> serverElements = DomUtils.getChildElementsByTagName(serversElement, "server");
            if (!serverElements.isEmpty()) {
                for (Element serverElement : serverElements) {
                    servers.add(new RuntimeBeanReference(serverElement.getAttribute("name")));
                }

                beanDefinition.addPropertyValue("serverList", servers);
            }
        }

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class StartServerActionFactoryBean extends AbstractTestActionFactoryBean<StartServerAction, StartServerAction.Builder> {

        private final StartServerAction.Builder builder = new StartServerAction.Builder();

        public void setServerList(List<Server> serverList) {
            serverList.forEach(builder::server);
        }

        public void setServer(Server server) {
            builder.server(server);
        }

        @Override
        public StartServerAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return StartServerAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public StartServerAction.Builder getBuilder() {
            return builder;
        }
    }
}
