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

package com.consol.citrus.ws.config.xml;

import com.consol.citrus.config.TestActionRegistry;
import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.DescriptionElementParser;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.ws.actions.AssertSoapFault;
import com.consol.citrus.ws.validation.SoapFaultDetailValidationContext;
import org.apache.xerces.util.DOMUtil;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Parser for SOAP fault assert action.
 * 
 * @author Christoph Deppisch
 */
public class AssertSoapFaultParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition;

        beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(AssertSoapFault.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("fault-code"), "faultCode");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("fault-string"), "faultString");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("fault-actor"), "faultActor");
        
        List<Element> faultDetails = DomUtils.getChildElementsByTagName(element, "fault-detail");
        SoapFaultDetailValidationContext validationContext = new SoapFaultDetailValidationContext();
        List<String> soapFaultDetails = new ArrayList<String>();
        List<String> soapFaultDetailPaths = new ArrayList<String>();
        for (Element faultDetailElement : faultDetails) {
            if (faultDetailElement.hasAttribute("file")) {
                if (StringUtils.hasText(DomUtils.getTextValue(faultDetailElement).trim())) {
                    throw new BeanCreationException("You tried to set fault-detail by file resource attribute and inline text value at the same time! " +
                            "Please choose one of them.");
                }

                String charset = faultDetailElement.getAttribute("charset");
                String filePath = faultDetailElement.getAttribute("file");
                soapFaultDetailPaths.add(filePath + (StringUtils.hasText(charset) ? FileUtils.FILE_PATH_CHARSET_PARAMETER + charset : ""));
            } else {
                String faultDetailData = DomUtils.getTextValue(faultDetailElement).trim();
                if (StringUtils.hasText(faultDetailData)) {
                    soapFaultDetails.add(faultDetailData);
                } else {
                    throw new BeanCreationException("Not content for fault-detail is set! Either use file attribute or inline text value for fault-detail element.");
                }
            }
            
            XmlMessageValidationContext context = new XmlMessageValidationContext();
            String schemaValidation = faultDetailElement.getAttribute("schema-validation");
            if (StringUtils.hasText(schemaValidation)) {
                context.setSchemaValidation(Boolean.valueOf(schemaValidation));
            }
            
            String schema = faultDetailElement.getAttribute("schema");
            if (StringUtils.hasText(schema)) {
                context.setSchema(schema);
            }
            
            String schemaRepository = faultDetailElement.getAttribute("schema-repository");
            if (StringUtils.hasText(schemaRepository)) {
                context.setSchemaRepository(schemaRepository);
            }
            validationContext.addValidationContext(context);
        }
        
        if (!soapFaultDetails.isEmpty() || !soapFaultDetailPaths.isEmpty()) {
            beanDefinition.addPropertyValue("faultDetails", soapFaultDetails);
            beanDefinition.addPropertyValue("faultDetailResourcePaths", soapFaultDetailPaths);
            beanDefinition.addPropertyValue("validationContext", validationContext);
        }
        
        Map<String, BeanDefinitionParser> actionRegistry = TestActionRegistry.getRegisteredActionParser();
        Element action = DOMUtil.getFirstChildElement(DomUtils.getChildElementByTagName(element, "when"));
        if (action != null) {
            BeanDefinitionParser parser = actionRegistry.get(action.getTagName());
            
            if (parser ==  null) {
            	beanDefinition.addPropertyValue("action", parserContext.getReaderContext().getNamespaceHandlerResolver().resolve(action.getNamespaceURI()).parse(action, parserContext));
            } else {
            	beanDefinition.addPropertyValue("action", parser.parse(action, parserContext));
            }
        }

        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("fault-validator"), "validator", "soapFaultValidator");

        return beanDefinition.getBeanDefinition();
    }
}
