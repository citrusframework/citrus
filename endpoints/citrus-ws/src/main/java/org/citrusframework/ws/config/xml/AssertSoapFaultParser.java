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

package org.citrusframework.ws.config.xml;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestAction;
import org.citrusframework.config.CitrusNamespaceParserRegistry;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractTestActionFactoryBean;
import org.citrusframework.config.xml.DescriptionElementParser;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.ws.actions.AssertSoapFault;
import org.citrusframework.ws.validation.SoapFaultDetailValidationContext;
import org.citrusframework.ws.validation.SoapFaultValidationContext;
import org.citrusframework.ws.validation.SoapFaultValidator;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Parser for SOAP fault assert action.
 *
 * @author Christoph Deppisch
 */
public class AssertSoapFaultParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition;

        beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(AssertSoapFaultActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("fault-code"), "faultCode");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("fault-string"), "faultString");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("fault-actor"), "faultActor");

        List<Element> faultDetails = DomUtils.getChildElementsByTagName(element, "fault-detail");
        SoapFaultValidationContext.Builder validationContext = new SoapFaultValidationContext.Builder();
        List<String> soapFaultDetails = new ArrayList<>();
        List<String> soapFaultDetailPaths = new ArrayList<>();
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

            SoapFaultDetailValidationContext.Builder context = new SoapFaultDetailValidationContext.Builder();
            String schemaValidation = faultDetailElement.getAttribute("schema-validation");
            if (StringUtils.hasText(schemaValidation)) {
                context.schemaValidation(Boolean.parseBoolean(schemaValidation));
            }

            String schema = faultDetailElement.getAttribute("schema");
            if (StringUtils.hasText(schema)) {
                context.schema(schema);
            }

            String schemaRepository = faultDetailElement.getAttribute("schema-repository");
            if (StringUtils.hasText(schemaRepository)) {
                context.schemaRepository(schemaRepository);
            }
            validationContext.detail(context.build());
        }

        if (!soapFaultDetails.isEmpty() || !soapFaultDetailPaths.isEmpty()) {
            beanDefinition.addPropertyValue("faultDetails", soapFaultDetails);
            beanDefinition.addPropertyValue("faultDetailResourcePaths", soapFaultDetailPaths);
            beanDefinition.addPropertyValue("validationContext", validationContext);
        }

        Element when = DomUtils.getChildElementByTagName(element, "when");
        if (when != null) {
            Element action = DomUtils.getChildElements(when).stream().findFirst().orElse(null);
            if (action != null) {
                BeanDefinitionParser parser = null;
                if (action.getNamespaceURI().equals("http://www.citrusframework.org/schema/testcase")) {
                    parser = CitrusNamespaceParserRegistry.getBeanParser(action.getLocalName());
                }

                if (parser == null) {
                    beanDefinition.addPropertyValue("action", parserContext.getReaderContext().getNamespaceHandlerResolver().resolve(action.getNamespaceURI()).parse(action, parserContext));
                } else {
                    beanDefinition.addPropertyValue("action", parser.parse(action, parserContext));
                }
            }
        }

        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("fault-validator"), "validator", "soapFaultValidator");

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class AssertSoapFaultActionFactoryBean extends AbstractTestActionFactoryBean<AssertSoapFault, AssertSoapFault.Builder> {

        private final AssertSoapFault.Builder builder = new AssertSoapFault.Builder();

        /**
         * Sets the nested test action.
         * @param action
         */
        public void setAction(TestAction action) {
            builder.when(action);
        }

        /**
         * Set the fault code.
         * @param faultCode the faultCode to set
         */
        public void setFaultCode(String faultCode) {
            builder.faultCode(faultCode);
        }

        /**
         * Set the fault string.
         * @param faultString the faultString to set
         */
        public void setFaultString(String faultString) {
            builder.faultString(faultString);
        }

        /**
         * @param validator the validator to set
         */
        public void setValidator(SoapFaultValidator validator) {
            builder.validator(validator);
        }

        /**
         * Sets the faultDetails.
         * @param faultDetails the faultDetails to set
         */
        public void setFaultDetails(List<String> faultDetails) {
            faultDetails.forEach(builder::faultDetail);
        }

        /**
         * Sets the fault detail resource paths.
         * @param faultDetailResourcePaths
         */
        public void setFaultDetailResourcePaths(List<String> faultDetailResourcePaths) {
            faultDetailResourcePaths.forEach(builder::faultDetailResource);
        }

        /**
         * Sets the faultActor.
         * @param faultActor the faultActor to set
         */
        public void setFaultActor(String faultActor) {
            builder.faultActor(faultActor);
        }

        /**
         * Sets the validationContext.
         * @param validationContext the validationContext to set
         */
        public void setValidationContext(SoapFaultValidationContext.Builder validationContext) {
            builder.validate(validationContext);
        }

        @Override
        public AssertSoapFault getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return AssertSoapFault.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public AssertSoapFault.Builder getBuilder() {
            return builder;
        }
    }
}
