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

package org.citrusframework.config.xml;

import org.citrusframework.actions.TransformAction;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for transform action in test case.
 *
 * @author Philipp Komninos
 */
public class TransformActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(TransformActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        Element xmlDataElement = DomUtils.getChildElementByTagName(element, "xml-data");
        if (xmlDataElement != null) {
            beanDefinition.addPropertyValue("xmlData", DomUtils.getTextValue(xmlDataElement));
        }

        Element xmlResourceElement = DomUtils.getChildElementByTagName(element, "xml-resource");
        if (xmlResourceElement != null) {
            beanDefinition.addPropertyValue("xmlResourcePath", xmlResourceElement.getAttribute("file"));
            if (xmlResourceElement.hasAttribute("charset")) {
                beanDefinition.addPropertyValue("xmlResourceCharset", xmlResourceElement.getAttribute("charset"));
            }
        }

        Element xsltDataElement = DomUtils.getChildElementByTagName(element, "xslt-data");
        if (xsltDataElement != null) {
            beanDefinition.addPropertyValue("xsltData", DomUtils.getTextValue(xsltDataElement));
        }

        Element xsltResourceElement = DomUtils.getChildElementByTagName(element, "xslt-resource");
        if (xsltResourceElement != null) {
            beanDefinition.addPropertyValue("xsltResourcePath", xsltResourceElement.getAttribute("file"));
            if (xsltResourceElement.hasAttribute("charset")) {
                beanDefinition.addPropertyValue("xsltResourceCharset", xsltResourceElement.getAttribute("charset"));
            }
        }

        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("variable"), "targetVariable");

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class TransformActionFactoryBean extends AbstractTestActionFactoryBean<TransformAction, TransformAction.Builder> {

        private final TransformAction.Builder builder = new TransformAction.Builder();

        /**
         * Set the XML document
         * @param xmlData the xmlData to set
         */
        public void setXmlData(String xmlData) {
            builder.source(xmlData);
        }

        /**
         * Set the XML document as resource
         * @param xmlResource the xmlResource to set
         */
        public void setXmlResourcePath(String xmlResource) {
            builder.sourceFile(xmlResource);
        }

        /**
         * Set the XSLT document
         * @param xsltData the xsltData to set
         */
        public void setXsltData(String xsltData) {
            builder.xslt(xsltData);
        }

        /**
         * Set the XSLT document as resource
         * @param xsltResource the xsltResource to set
         */
        public void setXsltResourcePath(String xsltResource) {
            builder.xsltFile(xsltResource);
        }

        /**
         * Set the target variable for the result
         * @param targetVariable the targetVariable to set
         */
        public void setTargetVariable(String targetVariable) {
            builder.result(targetVariable);
        }

        @Override
        public TransformAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return TransformAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public TransformAction.Builder getBuilder() {
            return builder;
        }
    }
}
