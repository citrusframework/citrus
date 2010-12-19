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

package com.consol.citrus.xml.namespace;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.variable.XpathPayloadVariableExtractor;

/**
 * Post processing all test actions and injecting central {@link NamespaceContextBuilder} into
 * {@link VariableExtractor} instances in {@link ReceiveMessageAction}.
 * 
 * {@link VariableExtractor} is not in Spring BeanContainer and therefore can not autowire {@link NamespaceContextBuilder}
 * as usual.
 * 
 * @author Christoph Deppisch
 */
public class NamespaceContextBeanPostProcessor implements BeanPostProcessor {
    
    @Autowired(required=false)
    NamespaceContextBuilder namespaceContextBuilder;

    /**
     * Checks for {@link VariableExtractor} instances and injects {@link NamespaceContextBuilder}
     * where applicable.
     */
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        if (namespaceContextBuilder != null && bean instanceof ReceiveMessageAction) {
            List<VariableExtractor> extractors = ((ReceiveMessageAction) bean).getVariableExtractors();
            for (VariableExtractor variableExtractor : extractors) {
                if (variableExtractor instanceof XpathPayloadVariableExtractor) {
                    ((XpathPayloadVariableExtractor) variableExtractor).setNamespaceContextBuilder(namespaceContextBuilder);
                }
            }
        }
        
        return bean;
    }

    /**
     * Just return the bean instance - do nothing here.
     */
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

}
