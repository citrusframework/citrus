package com.consol.citrus.schema;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.ExecutePLSQLBean;

public class ExecutePLSQLActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String parentBeanName = element.getAttribute("connect");
        BeanDefinitionBuilder beanDefinition;

        if (parentBeanName != null && parentBeanName.length()>0) {
            beanDefinition = BeanDefinitionBuilder.childBeanDefinition(parentBeanName);
            beanDefinition.addPropertyValue("name", element.getLocalName() + ":" + parentBeanName);
        } else {
            beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ExecutePLSQLBean.class);
            beanDefinition.addPropertyValue("name", element.getLocalName());
        }

        Element descriptionElement = DomUtils.getChildElementByTagName(element, "description");
        if (descriptionElement != null) {
            beanDefinition.addPropertyValue("description", DomUtils.getTextValue(descriptionElement).trim());
        }

        Element scriptElement = DomUtils.getChildElementByTagName(element, "script");
        if (scriptElement != null) {
            beanDefinition.addPropertyValue("script", DomUtils.getTextValue(scriptElement).trim());
        }

        Element sqlResourceElement = DomUtils.getChildElementByTagName(element, "resource");
        if (sqlResourceElement != null) {
            String filePath = sqlResourceElement.getAttribute("file");
            if (filePath.startsWith("classpath:")) {
                beanDefinition.addPropertyValue("sqlResource", new ClassPathResource(filePath.substring("classpath:".length())));
            } else if (filePath.startsWith("file:")) {
                beanDefinition.addPropertyValue("sqlResource", new FileSystemResource(filePath.substring("file:".length())));
            } else {
                beanDefinition.addPropertyValue("sqlResource", new FileSystemResource(filePath));
            }
        }

        String ignoreErrors = element.getAttribute("ignoreErrors");
        if (ignoreErrors != null && ignoreErrors.equals("true")) {
            beanDefinition.addPropertyValue("ignoreErrors", true);
        }

        return beanDefinition.getBeanDefinition();
    }
}
