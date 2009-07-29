package com.consol.citrus.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.SqlQueryBean;

public class SqlQueryActionParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String parentBeanName = element.getAttribute("connect");
        BeanDefinitionBuilder beanDefinition;

        if (StringUtils.hasText(parentBeanName)) {
            beanDefinition = BeanDefinitionBuilder.childBeanDefinition(parentBeanName);
            beanDefinition.addPropertyValue("name", element.getLocalName() + ":" + parentBeanName);
        } else {
            beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(SqlQueryBean.class);
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

        Map validateValues = new HashMap();
        List validateElements = DomUtils.getChildElementsByTagName(element, "validate");
        for (Iterator iter = validateElements.iterator(); iter.hasNext();) {
            Element validate = (Element) iter.next();
            validateValues.put(validate.getAttribute("column"), validate.getAttribute("value"));
        }
        beanDefinition.addPropertyValue("validationElements", validateValues);

        return beanDefinition.getBeanDefinition();
    }
}
