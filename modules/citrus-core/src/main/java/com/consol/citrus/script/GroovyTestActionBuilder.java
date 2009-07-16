package com.consol.citrus.script;

import java.util.Map;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.context.support.GenericApplicationContext;

import com.consol.citrus.TestAction;

public class GroovyTestActionBuilder {
    GenericApplicationContext applicationContext;
    
    public TestAction build(String name, Map properties) {
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
