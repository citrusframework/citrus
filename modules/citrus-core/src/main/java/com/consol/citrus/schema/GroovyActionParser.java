package com.consol.citrus.schema;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.script.GroovyScriptBean;

public class GroovyActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(GroovyScriptBean.class);
        
        if(DomUtils.getTextValue(element) != null && DomUtils.getTextValue(element).length() > 0) {
            beanDefinition.addPropertyValue("script", DomUtils.getTextValue(element));
        }
        
        String filePath = element.getAttribute("resource");
        if (filePath != null && filePath.length() > 0) {
            if (filePath.startsWith("classpath:")) {
                beanDefinition.addPropertyValue("fileResource", new ClassPathResource(filePath.substring("classpath:".length())));
            } else if (filePath.startsWith("file:")) {
                beanDefinition.addPropertyValue("fileResource", new FileSystemResource(filePath.substring("file:".length())));
            } else {
                beanDefinition.addPropertyValue("fileResource", new FileSystemResource(filePath));
            }
        }
        
        return beanDefinition.getBeanDefinition();
    }
}
