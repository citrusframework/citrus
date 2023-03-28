package org.citrusframework.config.xml;

import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.config.TestCaseFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.citrusframework.TestCaseMetaInfo.Status;

/**
 * Base meta info parser responsible for parsing the standard citrus TestCaseMetaInfos
 *
 * @param <T>
 *
 * @author Thorsten Schlathoelter
 */
public abstract class BaseTestCaseMetaInfoParser<T extends TestCaseMetaInfo> implements BeanDefinitionParser  {

    /**
     * The type of meta info
     */
    private final Class<T> metaInfoType;

    protected BaseTestCaseMetaInfoParser(Class<T> metaInfoType) {
        this.metaInfoType = metaInfoType;
    }

    @Override
    public BeanDefinition parse(Element metaInfoElement, ParserContext parserContext) {
        BeanDefinitionBuilder metaInfoBuilder = BeanDefinitionBuilder.rootBeanDefinition(metaInfoType);

        Element authorElement = DomUtils.getChildElementByTagName(metaInfoElement, "author");
        Element creationDateElement = DomUtils.getChildElementByTagName(metaInfoElement, "creationdate");
        Element statusElement = DomUtils.getChildElementByTagName(metaInfoElement, "status");
        Element lastUpdatedByElement = DomUtils.getChildElementByTagName(metaInfoElement, "last-updated-by");
        Element lastUpdatedOnElement = DomUtils.getChildElementByTagName(metaInfoElement, "last-updated-on");

        metaInfoBuilder.addPropertyValue("author", DomUtils.getTextValue(authorElement));
        try {
            metaInfoBuilder.addPropertyValue("creationDate", new SimpleDateFormat("yyyy-MM-dd").parse(DomUtils.getTextValue(creationDateElement)));
        } catch (ParseException e) {
            throw new BeanCreationException("Unable to parse creation date", e);
        }

        String status = DomUtils.getTextValue(statusElement);

        if (status.equals("DRAFT")) {
            metaInfoBuilder.addPropertyValue("status", Status.DRAFT);
        } else if (status.equals("READY_FOR_REVIEW")) {
            metaInfoBuilder.addPropertyValue("status", Status.READY_FOR_REVIEW);
        } else if (status.equals("FINAL")) {
            metaInfoBuilder.addPropertyValue("status", Status.FINAL);
        } else if (status.equals("DISABLED")) {
            metaInfoBuilder.addPropertyValue("status", Status.DISABLED);
        }

        if (lastUpdatedByElement != null) {
            metaInfoBuilder.addPropertyValue("lastUpdatedBy", DomUtils.getTextValue(lastUpdatedByElement));
        }

        if (lastUpdatedOnElement != null) {
            try {
                metaInfoBuilder.addPropertyValue("lastUpdatedOn", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(DomUtils.getTextValue(lastUpdatedOnElement)));
            } catch (ParseException e) {
                throw new BeanCreationException("Unable to parse lastupdate date", e);
            }
        }

        parseAdditionalProperties(metaInfoElement, metaInfoBuilder);

        return metaInfoBuilder.getBeanDefinition();
    }

    /**
     * Subclasses may override and add their specific properties
     * @param metaInfoBuilder
     */
    protected void parseAdditionalProperties(Element metaInfoElement, BeanDefinitionBuilder metaInfoBuilder) {
    }

}