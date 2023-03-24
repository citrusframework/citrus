/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.jmx.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractServerParser;
import org.citrusframework.jmx.endpoint.JmxEndpointConfiguration;
import org.citrusframework.jmx.model.*;
import org.citrusframework.jmx.server.JmxServer;
import org.citrusframework.server.AbstractServer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxServerParser extends AbstractServerParser {
    @Override
    protected void parseServer(BeanDefinitionBuilder serverBuilder, Element element, ParserContext parserContext) {
        BeanDefinitionBuilder configurationBuilder = BeanDefinitionBuilder.genericBeanDefinition(JmxEndpointConfiguration.class);
        BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("server-url"), "serverUrl");
        BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("host"), "host");
        BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("port"), "port");
        BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("protocol"), "protocol");
        BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("binding"), "binding");

        BeanDefinitionParserUtils.setPropertyValue(serverBuilder, element.getAttribute("create-registry"), "createRegistry");
        BeanDefinitionParserUtils.setPropertyReference(configurationBuilder, element.getAttribute("environment-properties"), "environmentProperties");
        BeanDefinitionParserUtils.setPropertyReference(configurationBuilder, element.getAttribute("message-converter"), "messageConverter");
        BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("timeout"), "timeout");

        Element mbeansElement = DomUtils.getChildElementByTagName(element, "mbeans");
        ManagedList<BeanDefinition> mbeans = new ManagedList<>();
        if (mbeansElement != null) {
            List<?> mbeanElement = DomUtils.getChildElementsByTagName(mbeansElement, "mbean");
            for (Object aMbeanElement : mbeanElement) {
                Element mbean = (Element) aMbeanElement;
                BeanDefinitionBuilder mbeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(ManagedBeanDefinition.class);
                BeanDefinitionParserUtils.setPropertyValue(mbeanDefinition, mbean.getAttribute("type"), "type");
                BeanDefinitionParserUtils.setPropertyValue(mbeanDefinition, mbean.getAttribute("name"), "name");
                BeanDefinitionParserUtils.setPropertyValue(mbeanDefinition, mbean.getAttribute("objectDomain"), "objectDomain");
                BeanDefinitionParserUtils.setPropertyValue(mbeanDefinition, mbean.getAttribute("objectName"), "objectName");

                Element operationsElement = DomUtils.getChildElementByTagName(mbean, "operations");
                if (operationsElement != null) {
                    List<?> operationElement = DomUtils.getChildElementsByTagName(operationsElement, "operation");
                    List<ManagedBeanInvocation.Operation> operationList = new ArrayList<>();
                    for (Object anOperationElement : operationElement) {
                        Element operation = (Element) anOperationElement;

                        ManagedBeanInvocation.Operation op = new ManagedBeanInvocation.Operation();
                        op.setName(operation.getAttribute("name"));

                        Element parameterElement = DomUtils.getChildElementByTagName(operation, "parameter");
                        if (parameterElement != null) {
                            op.setParameter(new ManagedBeanInvocation.Parameter());
                            List<?> paramElement = DomUtils.getChildElementsByTagName(parameterElement, "param");
                            for (Object aParamElement : paramElement) {
                                Element param = (Element) aParamElement;
                                OperationParam p = new OperationParam();
                                p.setType(param.getAttribute("type"));
                                op.getParameter().getParameter().add(p);
                            }
                        }

                        operationList.add(op);
                    }
                    mbeanDefinition.addPropertyValue("operations", operationList);
                }

                Element attributesElement = DomUtils.getChildElementByTagName(mbean, "attributes");
                if (attributesElement != null) {
                    List<?> attributeElement = DomUtils.getChildElementsByTagName(attributesElement, "attribute");
                    List<ManagedBeanInvocation.Attribute> attributeList = new ArrayList<>();
                    for (Object anAttributeElement : attributeElement) {
                        Element attribute = (Element) anAttributeElement;

                        ManagedBeanInvocation.Attribute att = new ManagedBeanInvocation.Attribute();
                        att.setType(attribute.getAttribute("type"));
                        att.setName(attribute.getAttribute("name"));
                        attributeList.add(att);
                    }
                    mbeanDefinition.addPropertyValue("attributes", attributeList);
                }

                mbeans.add(mbeanDefinition.getBeanDefinition());
            }

            serverBuilder.addPropertyValue("mbeans", mbeans);
        }

        String endpointConfigurationId = element.getAttribute(ID_ATTRIBUTE) + "Configuration";
        BeanDefinitionParserUtils.registerBean(endpointConfigurationId, configurationBuilder.getBeanDefinition(), parserContext, shouldFireEvents());

        serverBuilder.addConstructorArgReference(endpointConfigurationId);
    }

    @Override
    protected Class<? extends AbstractServer> getServerClass() {
        return JmxServer.class;
    }
}
