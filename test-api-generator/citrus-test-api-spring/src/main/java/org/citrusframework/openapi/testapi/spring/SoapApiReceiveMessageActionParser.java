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

import org.citrusframework.openapi.testapi.GeneratedApi;
import org.citrusframework.openapi.testapi.SoapApiReceiveMessageActionBuilder;
import org.citrusframework.util.StringUtils;
import org.citrusframework.ws.config.xml.ReceiveSoapMessageActionParser;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class SoapApiReceiveMessageActionParser extends ReceiveSoapMessageActionParser {

    /**
     * The generated api bean class.
     */
    private final Class<? extends GeneratedApi> apiBeanClass;

    /**
     * The builder class for the receive message action.
     */
    private final Class<? extends SoapApiReceiveMessageActionBuilder> receiveBeanClass;

    private final String defaultEndpointName;

    public SoapApiReceiveMessageActionParser(
        Class<? extends GeneratedApi> apiBeanClass,
        Class<? extends SoapApiReceiveMessageActionBuilder> beanClass,
        String defaultEndpointName) {
        this.apiBeanClass = apiBeanClass;
        this.receiveBeanClass = beanClass;
        this.defaultEndpointName = defaultEndpointName;
    }

    @Override
    protected BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinitionBuilder = super.parseComponent(element, parserContext);

        BeanDefinitionBuilder actionBuilder = createTestApiActionBuilder();
        beanDefinitionBuilder.addConstructorArgValue(actionBuilder.getBeanDefinition());

        return beanDefinitionBuilder;
    }

    protected String parseEndpoint(Element element) {
        String endpointUri = element.getAttribute("endpoint");

        if (!StringUtils.hasText(endpointUri)) {
            endpointUri = defaultEndpointName;
        }
        return endpointUri;
    }

    private BeanDefinitionBuilder createTestApiActionBuilder() {

        BeanDefinitionBuilder actionBuilder = BeanDefinitionBuilder.genericBeanDefinition(
            receiveBeanClass);
        actionBuilder.addConstructorArgValue(new RuntimeBeanReference(apiBeanClass));

        return actionBuilder;
    }

    @Override
    protected Class<TestApiSoapClientReceiveActionBuilderFactoryBean> getMessageFactoryClass() {
        return TestApiSoapClientReceiveActionBuilderFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static class TestApiSoapClientReceiveActionBuilderFactoryBean extends
        ReceiveSoapMessageActionFactoryBean {

        public TestApiSoapClientReceiveActionBuilderFactoryBean(
            SoapApiReceiveMessageActionBuilder builder) {
            super(builder);
        }
    }

}
