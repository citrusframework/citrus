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

package com.consol.citrus.script;

import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.context.support.GenericApplicationContext;

import com.consol.citrus.TestAction;

/**
 * Build test action object from groovy script.
 * 
 * @author Christoph Deppisch
 */
public class GroovyTestActionBuilder {
    /** Application context */
    private GenericApplicationContext applicationContext;
    
    /**
     * Build the test action object.
     * @param name
     * @param properties
     * @return
     */
    public TestAction build(String name, Map<String, Object> properties) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.childBeanDefinition(name);
        
        if(name.startsWith("send")) {
            beanDefinition.addPropertyValue("headerValues", properties.get("header"));
            beanDefinition.addPropertyValue("messageData", properties.get("msg").toString());
            
            if(properties.get("replace") != null) {
                beanDefinition.addPropertyValue("messageElements", properties.get("replace"));
            }
        } else if(name.startsWith("receive")) {
            beanDefinition.addPropertyValue("headerValues", properties.get("header"));
            beanDefinition.addPropertyValue("messageData", properties.get("msg").toString());
            
            if(properties.get("replace") != null) {
                beanDefinition.addPropertyValue("messageElements", properties.get("replace"));
            }
        }
        
        String beanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition.getBeanDefinition(), applicationContext);
        
        applicationContext.registerBeanDefinition(beanName, beanDefinition.getBeanDefinition());
        
        return (TestAction)applicationContext.getBean(beanName, TestAction.class);
    }

    /**
     * Set the application context.
     * @param applicationContext the applicationContext to set
     */
    public void setApplicationContext(GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
