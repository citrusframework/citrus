package com.consol.citrus.schema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.ExecuteSqlBean;

public class ExecuteSqlActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String parentBeanName = element.getAttribute("connect");
        BeanDefinitionBuilder beanDefinition;

        if (parentBeanName != null && parentBeanName.length()>0) {
            beanDefinition = BeanDefinitionBuilder.childBeanDefinition(parentBeanName);
            beanDefinition.addPropertyValue("name", element.getLocalName() + ":" + parentBeanName);
        } else {
            beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ExecuteSqlBean.class);
            beanDefinition.addPropertyValue("name", element.getLocalName());
        }

        DescriptionElementParser.doParse(element, beanDefinition);

        List statements = new ArrayList();
        List stmtElements = DomUtils.getChildElementsByTagName(element, "statement");
        for (Iterator iter = stmtElements.iterator(); iter.hasNext();) {
            Element stmt = (Element) iter.next();
            statements.add(DomUtils.getTextValue(stmt));
        }
        beanDefinition.addPropertyValue("statements", statements);

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
