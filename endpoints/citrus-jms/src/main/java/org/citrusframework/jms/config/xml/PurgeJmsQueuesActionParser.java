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

package org.citrusframework.jms.config.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractTestActionFactoryBean;
import org.citrusframework.config.xml.DescriptionElementParser;
import org.citrusframework.jms.actions.PurgeJmsQueuesAction;
import org.citrusframework.util.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for purge-jms-queues action in test case.
 *
 * @author Christoph Deppisch
 */
public class PurgeJmsQueuesActionParser implements BeanDefinitionParser {

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(PurgeJmsQueuesActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        String connectionFactory = "connectionFactory"; //default value

        if (element.hasAttribute("connection-factory")) {
            connectionFactory = element.getAttribute("connection-factory");
        }

        if (!StringUtils.hasText(connectionFactory)) {
            parserContext.getReaderContext().error("Attribute 'connection-factory' must not be empty", element);
        }

        beanDefinition.addPropertyReference("connectionFactory", connectionFactory);

        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("timeout"), "receiveTimeout");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("sleep"), "sleepTime");

        List<String> queueNames = new ArrayList<String>();
        ManagedList<BeanDefinition> queueRefs = new ManagedList<BeanDefinition>();
        List<Element> queueElements = DomUtils.getChildElementsByTagName(element, "queue");
        for (Element queue : queueElements) {
            String queueName = queue.getAttribute("name");
            String queueRef = queue.getAttribute("ref");

            if (StringUtils.hasText(queueName)) {
                queueNames.add(queueName);
            } else if (StringUtils.hasText(queueRef)) {
                queueRefs.add(BeanDefinitionBuilder.childBeanDefinition(queueRef).getBeanDefinition());
            } else {
                throw new BeanCreationException("Element 'queue' must set one of the attributes 'name' or 'ref'");
            }
        }

        beanDefinition.addPropertyValue("queueNames", queueNames);
        beanDefinition.addPropertyValue("queues", queueRefs);

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class PurgeJmsQueuesActionFactoryBean extends AbstractTestActionFactoryBean<PurgeJmsQueuesAction, PurgeJmsQueuesAction.Builder> {

        private final PurgeJmsQueuesAction.Builder builder = new PurgeJmsQueuesAction.Builder();

        /**
         * List of queue names to purge.
         * @param queueNames the queueNames to set
         */
        public void setQueueNames(List<String> queueNames) {
            builder.queueNames(queueNames);
        }

        /**
         * Connection factory.
         * @param connectionFactory the connectionFactory to set
         */
        public void setConnectionFactory(ConnectionFactory connectionFactory) {
            builder.connectionFactory(connectionFactory);
        }

        /**
         * List of queues.
         * @param queues The queues which are to be purged.
         */
        public void setQueues(List<Queue> queues) {
            builder.queues(queues);
        }

        /**
         * Receive timeout for reading message from a destination.
         * @param receiveTimeout the receiveTimeout to set
         */
        public void setReceiveTimeout(long receiveTimeout) {
            builder.timeout(receiveTimeout);
        }

        /**
         * Sets the sleepTime.
         * @param sleepTime the sleepTime to set
         */
        public void setSleepTime(long sleepTime) {
            builder.sleep(sleepTime);
        }

        @Override
        public PurgeJmsQueuesAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return PurgeJmsQueuesAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public PurgeJmsQueuesAction.Builder getBuilder() {
            return builder;
        }
    }
}
