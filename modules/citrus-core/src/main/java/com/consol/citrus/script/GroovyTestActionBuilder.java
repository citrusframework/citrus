/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.script;

import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.context.support.GenericApplicationContext;

import com.consol.citrus.TestAction;

public class GroovyTestActionBuilder {
    private GenericApplicationContext applicationContext;
    
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
     * @param applicationContext the applicationContext to set
     */
    public void setApplicationContext(GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
