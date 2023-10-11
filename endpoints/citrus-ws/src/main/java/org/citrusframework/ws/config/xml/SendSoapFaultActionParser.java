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

import org.citrusframework.config.xml.AbstractSendMessageActionFactoryBean;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.ws.actions.SendSoapFaultAction;
import org.citrusframework.ws.message.SoapAttachment;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for send soap fault action in test case.
 *
 * @author Christoph Deppisch
 */
public class SendSoapFaultActionParser extends SendSoapMessageActionParser {

    @Override
    public BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = super.parseComponent(element, parserContext);

        parseFault(builder, DomUtils.getChildElementByTagName(element, "fault"));

        return builder;
    }

    /**
     * Parses the SOAP fault information.
     * @param builder
     * @param faultElement
     */
    private void parseFault(BeanDefinitionBuilder builder, Element faultElement) {
        if (faultElement != null) {
            Element faultCodeElement = DomUtils.getChildElementByTagName(faultElement, "fault-code");
            if (faultCodeElement != null) {
                builder.addPropertyValue("faultCode", DomUtils.getTextValue(faultCodeElement).trim());
            }

            Element faultStringElement = DomUtils.getChildElementByTagName(faultElement, "fault-string");
            if (faultStringElement != null) {
                builder.addPropertyValue("faultString", DomUtils.getTextValue(faultStringElement).trim());
            }

            Element faultActorElement = DomUtils.getChildElementByTagName(faultElement, "fault-actor");
            if (faultActorElement != null) {
                builder.addPropertyValue("faultActor", DomUtils.getTextValue(faultActorElement).trim());
            }

            parseFaultDetail(builder, faultElement);
        }
    }

    /**
     * Parses the fault detail element.
     *
     * @param builder
     * @param faultElement the fault DOM element.
     */
    private void parseFaultDetail(BeanDefinitionBuilder builder, Element faultElement) {
        List<Element> faultDetailElements = DomUtils.getChildElementsByTagName(faultElement, "fault-detail");
        List<String> faultDetails = new ArrayList<String>();
        List<String> faultDetailResourcePaths = new ArrayList<String>();

        for (Element faultDetailElement : faultDetailElements) {
            if (faultDetailElement.hasAttribute("file")) {

                if (StringUtils.hasText(DomUtils.getTextValue(faultDetailElement).trim())) {
                    throw new BeanCreationException("You tried to set fault-detail by file resource attribute and inline text value at the same time! " +
                            "Please choose one of them.");
                }

                String charset = faultDetailElement.getAttribute("charset");
                String filePath = faultDetailElement.getAttribute("file");
                faultDetailResourcePaths.add(filePath + (StringUtils.hasText(charset) ? FileUtils.FILE_PATH_CHARSET_PARAMETER + charset : ""));
            } else {
                String faultDetailData = DomUtils.getTextValue(faultDetailElement).trim();
                if (StringUtils.hasText(faultDetailData)) {
                    faultDetails.add(faultDetailData);
                } else {
                    throw new BeanCreationException("Not content for fault-detail is set! Either use file attribute or inline text value for fault-detail element.");
                }
            }
        }

        builder.addPropertyValue("faultDetails", faultDetails);
        builder.addPropertyValue("faultDetailResourcePaths", faultDetailResourcePaths);
    }

    @Override
    protected Class<SendSoapFaultActionFactoryBean> getBeanDefinitionClass() {
        return SendSoapFaultActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static class SendSoapFaultActionFactoryBean extends AbstractSendMessageActionFactoryBean<SendSoapFaultAction, SendSoapFaultAction.Builder.SoapFaultMessageBuilderSupport, SendSoapFaultAction.Builder> {

        private final SendSoapFaultAction.Builder builder = new SendSoapFaultAction.Builder();

        /**
         * Sets the control attachments.
         * @param attachments the control attachments
         */
        public void setAttachments(List<SoapAttachment> attachments) {
            attachments.forEach(builder.message()::attachment);
        }

        /**
         * Enable or disable mtom attachments
         * @param mtomEnabled
         */
        public void setMtomEnabled(boolean mtomEnabled) {
            builder.message().mtomEnabled(mtomEnabled);
        }

        /**
         * Set the fault code QName string. This can be either
         * a fault code in {@link org.springframework.ws.soap.server.endpoint.SoapFaultDefinition}
         * or a custom QName like {http://citrusframework.org}citrus:TEC-1000
         *
         * @param faultCode the faultCode to set
         */
        public void setFaultCode(String faultCode) {
            builder.message().faultCode(faultCode);
        }

        /**
         * Set the fault reason string describing the fault.
         * @param faultString the faultString to set
         */
        public void setFaultString(String faultString) {
            builder.message().faultString(faultString);
        }

        /**
         * Sets the faultActor.
         * @param faultActor the faultActor to set
         */
        public void setFaultActor(String faultActor) {
            builder.message().faultActor(faultActor);
        }

        /**
         * Sets the faultDetails.
         * @param faultDetails the faultDetails to set
         */
        public void setFaultDetails(List<String> faultDetails) {
            faultDetails.forEach(builder.message()::faultDetail);
        }

        /**
         * Sets the fault detail resource paths.
         * @param faultDetailResourcePaths
         */
        public void setFaultDetailResourcePaths(List<String> faultDetailResourcePaths) {
            faultDetailResourcePaths.forEach(builder.message()::faultDetailResource);
        }

        @Override
        public SendSoapFaultAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return SendSoapFaultAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public SendSoapFaultAction.Builder getBuilder() {
            return builder;
        }
    }
}
