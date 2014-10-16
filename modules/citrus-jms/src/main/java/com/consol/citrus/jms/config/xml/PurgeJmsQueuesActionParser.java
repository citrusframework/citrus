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

package com.consol.citrus.jms.config.xml;

import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.DescriptionElementParser;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Bean definition parser for purge-jms-queues action in test case.
 * 
 * @author Christoph Deppisch
 */
public class PurgeJmsQueuesActionParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(PurgeJmsQueuesAction.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        String connectionFactory = "connectionFactory"; //default value
        
        if (element.hasAttribute("connection-factory")) {
            connectionFactory = element.getAttribute("connection-factory");
        }
        
        if (!StringUtils.hasText(connectionFactory)) {
            parserContext.getReaderContext().error("Attribute 'connection-factory' must not be empty", element);
        }
        
        beanDefinition.addPropertyReference("connectionFactory", connectionFactory);
        
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("receive-timeout"), "receiveTimeout");
        
        List<String> queueNames = new ArrayList<String>();
        ManagedList<BeanDefinition> queueRefs = new ManagedList<BeanDefinition>();
        List<?> queueElements = DomUtils.getChildElementsByTagName(element, "queue");
        for (Iterator<?> iter = queueElements.iterator(); iter.hasNext();) {
            Element queue = (Element) iter.next();
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
}
