/*
 * Copyright the original author or authors.
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

package org.citrusframework.openapi.testapi.spring;

import org.citrusframework.config.xml.AbstractTestContainerFactoryBean;
import org.citrusframework.config.xml.AsyncParser.AsyncFactoryBean;
import org.citrusframework.config.xml.SequenceParser.SequenceFactoryBean;
import org.citrusframework.openapi.testapi.GeneratedApi;
import org.citrusframework.openapi.testapi.SoapApiReceiveMessageActionBuilder;
import org.citrusframework.openapi.testapi.SoapApiSendMessageActionBuilder;
import org.citrusframework.util.StringUtils;
import org.citrusframework.ws.config.xml.SendSoapMessageActionParser;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import static java.lang.Boolean.parseBoolean;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.springframework.util.xml.DomUtils.getChildElementByTagName;

public class SoapApiSendMessageActionParser extends SendSoapMessageActionParser {

    /**
     * The generated api bean class.
     */
    private final Class<? extends GeneratedApi> apiBeanClass;

    /**
     * The builder class for the send message action.
     */
    private final Class<? extends SoapApiSendMessageActionBuilder> sendBeanClass;

    /**
     * The builder class for the receive message action, required when using nested send/receive xml
     * elements.
     */
    private final Class<? extends SoapApiReceiveMessageActionBuilder> receiveBeanClass;

    private final String defaultEndpointName;

    public SoapApiSendMessageActionParser(
            Class<? extends GeneratedApi> apiBeanClass,
            Class<? extends SoapApiSendMessageActionBuilder> sendBeanClass,
            Class<? extends SoapApiReceiveMessageActionBuilder> receiveBeanClass,
            String defaultEndpointName) {
        this.apiBeanClass = apiBeanClass;
        this.sendBeanClass = sendBeanClass;
        this.receiveBeanClass = receiveBeanClass;
        this.defaultEndpointName = defaultEndpointName;
    }

    @Override
    public BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinitionBuilder = super.parseComponent(element, parserContext);

        BeanDefinitionBuilder actionBuilder = createTestApiActionBuilder();
        beanDefinitionBuilder.addConstructorArgValue(actionBuilder.getBeanDefinition());

        Element receive = getChildElementByTagName(element, "receive");
        if (receive != null) {
            boolean fork = parseBoolean(element.getAttribute("fork"));
            return wrapSendAndReceiveActionInSequence(fork, receive, parserContext, beanDefinitionBuilder);
        }

        return beanDefinitionBuilder;
    }

    @Override
    protected String parseEndpoint(Element element) {
        String endpointUri = element.getAttribute("endpoint");

        if (!StringUtils.hasText(endpointUri)) {
            endpointUri = defaultEndpointName;
        }

        return endpointUri;
    }

    private BeanDefinitionBuilder createTestApiActionBuilder() {
        BeanDefinitionBuilder actionBuilder = genericBeanDefinition(sendBeanClass);
        actionBuilder.addConstructorArgValue(new RuntimeBeanReference(apiBeanClass));

        return actionBuilder;
    }

    @Override
    protected Class<TestApiSoapClientSendActionBuilderFactoryBean> getMessageFactoryClass() {
        return TestApiSoapClientSendActionBuilderFactoryBean.class;
    }

    /**
     * Handles the configuration for both sending and receiving actions when a nested <receive>
     * element is present in the XML specification. It creates appropriate builders for both sending
     * and receiving messages and adds them to a container that executes these actions in sequence
     * or asynchronously, depending on the {@code fork} parameter.
     */
    private BeanDefinitionBuilder wrapSendAndReceiveActionInSequence(boolean fork,
                                                                     Element receive,
                                                                     ParserContext parserContext,
                                                                     BeanDefinitionBuilder beanDefinitionBuilder) {
        Class<? extends AbstractTestContainerFactoryBean<?, ?>> containerClass = fork ? AsyncFactoryBean.class
                : SequenceFactoryBean.class;

        BeanDefinitionBuilder sequenceBuilder = genericBeanDefinition(containerClass);

        SoapApiReceiveMessageActionParser receiveApiResponseActionParser = new SoapApiReceiveMessageActionParser(
                apiBeanClass, receiveBeanClass, defaultEndpointName);
        BeanDefinition receiveResponseBeanDefinition = receiveApiResponseActionParser.parse(receive, parserContext);

        ManagedList<BeanDefinition> actions = new ManagedList<>();
        actions.add(beanDefinitionBuilder.getBeanDefinition());
        actions.add(receiveResponseBeanDefinition);

        sequenceBuilder.addPropertyValue("actions", actions);

        return sequenceBuilder;
    }

    /**
     * Test action factory bean.
     */
    public static class TestApiSoapClientSendActionBuilderFactoryBean extends
            SendSoapMessageActionFactoryBean {

        public TestApiSoapClientSendActionBuilderFactoryBean(SoapApiSendMessageActionBuilder builder) {
            super(builder);
        }
    }
}
