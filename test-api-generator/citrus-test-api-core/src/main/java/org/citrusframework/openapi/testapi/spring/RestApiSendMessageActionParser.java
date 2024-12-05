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

import static java.lang.Boolean.parseBoolean;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.citrusframework.openapi.testapi.TestApiUtils.mapXmlAttributeNameToJavaPropertyName;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.springframework.util.xml.DomUtils.getChildElementByTagName;

import java.util.List;
import java.util.stream.Collectors;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.config.xml.AbstractSendMessageActionFactoryBean;
import org.citrusframework.config.xml.AbstractTestContainerFactoryBean;
import org.citrusframework.config.xml.AsyncParser.AsyncFactoryBean;
import org.citrusframework.config.xml.SequenceParser.SequenceFactoryBean;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder.HttpMessageBuilderSupport;
import org.citrusframework.http.config.xml.HttpSendRequestActionParser;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.actions.OpenApiClientRequestActionBuilder.OpenApiClientRequestMessageBuilder;
import org.citrusframework.openapi.actions.OpenApiSpecificationSource;
import org.citrusframework.openapi.testapi.GeneratedApi;
import org.citrusframework.openapi.testapi.RestApiReceiveMessageActionBuilder;
import org.citrusframework.openapi.testapi.RestApiSendMessageActionBuilder;
import org.citrusframework.openapi.testapi.RestApiSendMessageActionBuilder.TestApiClientRequestMessageBuilder;
import org.citrusframework.util.StringUtils;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Parses the XML configuration for sending API requests based on OpenAPI specifications. Extends
 * {@link HttpSendRequestActionParser} to handle OpenAPI specific request and response builders.
 */
public class RestApiSendMessageActionParser extends HttpSendRequestActionParser {

    public static final String ENDPOINT_URI = "endpointUri";
    public static final String ENDPOINT = "endpoint";
    /**
     * The generated api bean class.
     */
    private final Class<? extends GeneratedApi> apiBeanClass;

    /**
     * The builder class for the send message action.
     */
    private final Class<? extends RestApiSendMessageActionBuilder> requestBeanClass;

    /**
     * The builder class for the receive message action, required when using nested send/receive xml
     * elements.
     */
    private final Class<? extends RestApiReceiveMessageActionBuilder> receiveBeanClass;

    /**
     * The OpenAPI specification that relates to the TestAPI classes.
     */
    private final OpenApiSpecification openApiSpecification;

    /**
     * The OpenAPI operationId, related to this parser
     */
    private final String operationId;

    /**
     * The OpenAPI operation path.
     */
    private final String path;
    private final String defaultEndpointName;
    /**
     * Constructor parameters for the requestBeanClass.
     */
    private List<String> constructorParameters = emptyList();
    /**
     * Optional non constructor parameters for the requestBeanClass.
     */
    private List<String> nonConstructorParameters = emptyList();

    public RestApiSendMessageActionParser(
        OpenApiSpecification openApiSpecification,
        String operationId,
        String path,
        Class<? extends GeneratedApi> apiBeanClass,
        Class<? extends RestApiSendMessageActionBuilder> sendBeanClass,
        Class<? extends RestApiReceiveMessageActionBuilder> receiveBeanClass,
        String defaultEndpointName) {
        this.openApiSpecification = openApiSpecification;
        this.operationId = operationId;
        this.path = path;
        this.apiBeanClass = apiBeanClass;
        this.requestBeanClass = sendBeanClass;
        this.receiveBeanClass = receiveBeanClass;
        this.defaultEndpointName = defaultEndpointName;
    }

    private static List<String> collectChildNodeContents(Element element, String parameterName) {
        return DomUtils.getChildElementsByTagName(element, parameterName)
            .stream()
            .map(Node::getTextContent)
            .filter(StringUtils::isNotEmpty)
            .collect(Collectors.toList()); // For further processing, list must not be immutable
    }

    @Override
    protected BeanDefinitionBuilder createBeanDefinitionBuilder(final Element element,
        ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinitionBuilder = super.createBeanDefinitionBuilder(element,
            parserContext);

        BeanDefinitionBuilder actionBuilder = createTestApiActionBuilder(element,
            beanDefinitionBuilder);
        beanDefinitionBuilder.addConstructorArgValue(actionBuilder.getBeanDefinition());

        setDefaultEndpoint(beanDefinitionBuilder);

        Element receive = getChildElementByTagName(element, "receive");
        if (receive != null) {
            boolean fork = parseBoolean(element.getAttribute("fork"));
            return wrapSendAndReceiveActionInSequence(fork, receive, parserContext,
                beanDefinitionBuilder);
        }

        return beanDefinitionBuilder;
    }

    private BeanDefinitionBuilder createTestApiActionBuilder(Element element,
        BeanDefinitionBuilder beanDefinitionBuilder) {
        BeanDefinitionBuilder actionBuilder = propagateMessageBuilderToActionBuilder(
            beanDefinitionBuilder);
        readConstructorParameters(element, actionBuilder);
        readNonConstructorParameters(element, actionBuilder);
        return actionBuilder;
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
        BeanDefinitionBuilder sendActionBeanDefinitionBuilder) {

        Class<? extends AbstractTestContainerFactoryBean<?, ?>> containerClass = createSequenceContainer(
            fork);

        BeanDefinitionBuilder sequenceBuilder = genericBeanDefinition(containerClass);

        RestApiReceiveMessageActionParser receiveApiResponseActionParser = getRestApiReceiveMessageActionParser(
            sendActionBeanDefinitionBuilder);

        BeanDefinition receiveResponseBeanDefinition = receiveApiResponseActionParser.parse(receive,
            parserContext);

        // Nested elements do not have reasonable names.
        String sendName = (String) sendActionBeanDefinitionBuilder.getBeanDefinition().getPropertyValues()
            .get("name");
        if (sendName != null) {
            String receiveName = sendName.replace(":send", ":receive");
            if (!sendName.equals(receiveName)) {
                receiveResponseBeanDefinition.getPropertyValues().add("name", receiveName);
            }
        }

        ManagedList<BeanDefinition> actions = new ManagedList<>();
        actions.add(sendActionBeanDefinitionBuilder.getBeanDefinition());
        actions.add(receiveResponseBeanDefinition);

        sequenceBuilder.addPropertyValue("actions", actions);

        return sequenceBuilder;
    }

    protected Class<? extends AbstractTestContainerFactoryBean<?, ?>> createSequenceContainer(boolean fork) {
        return fork ? AsyncFactoryBean.class : SequenceFactoryBean.class;
    }

    private RestApiReceiveMessageActionParser getRestApiReceiveMessageActionParser(
        BeanDefinitionBuilder sendBuilder) {

        return new RestApiReceiveMessageActionParser(openApiSpecification, operationId,
            apiBeanClass, receiveBeanClass, defaultEndpointName) {
            @Override
            protected void setDefaultEndpoint(BeanDefinitionBuilder beanDefinitionBuilder) {
                BeanDefinition beanDefinition = sendBuilder.getBeanDefinition();
                PropertyValue endpoint = beanDefinition.getPropertyValues()
                    .getPropertyValue(ENDPOINT);
                PropertyValue endpointUri = beanDefinition.getPropertyValues()
                    .getPropertyValue(ENDPOINT_URI);

                String receiveEndpointName = null;
                String receiveEndpointUri = null;

                if (endpoint != null
                    && endpoint.getValue() instanceof BeanReference beanReference) {
                    receiveEndpointName = beanReference.getBeanName();
                } else if (endpointUri != null && endpointUri.getValue() instanceof String uri) {
                    receiveEndpointUri = uri;
                }

                if (!beanDefinitionBuilder.getBeanDefinition().getPropertyValues()
                    .contains(ENDPOINT) &&
                    !beanDefinitionBuilder.getBeanDefinition().getPropertyValues()
                        .contains(ENDPOINT_URI)) {
                    if (receiveEndpointName != null) {
                        beanDefinitionBuilder.addPropertyReference(ENDPOINT, receiveEndpointName);
                    } else if (receiveEndpointUri != null) {
                        beanDefinitionBuilder.addPropertyValue(ENDPOINT_URI, receiveEndpointUri);
                    } else {
                        beanDefinitionBuilder.addPropertyReference(ENDPOINT, defaultEndpointName);
                    }
                }
            }
        };
    }

    /**
     * Propagates the message builder created by the superclass into the specific send message
     * action builder.
     */
    private BeanDefinitionBuilder propagateMessageBuilderToActionBuilder(
        BeanDefinitionBuilder beanDefinitionBuilder) {
        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        OpenApiClientRequestMessageBuilder messageBuilder = (OpenApiClientRequestMessageBuilder) beanDefinition.getPropertyValues()
            .get("messageBuilder");
        beanDefinition.getPropertyValues().removePropertyValue("messageBuilder");

        BeanDefinitionBuilder actionBuilder = genericBeanDefinition(requestBeanClass);

        actionBuilder.addConstructorArgValue(new RuntimeBeanReference(apiBeanClass));
        actionBuilder.addConstructorArgValue(messageBuilder);

        return actionBuilder;
    }

    /**
     * Reads constructor parameters from the XML element and adds them as constructor arguments to
     * the provided {@link BeanDefinitionBuilder}.
     */
    private void readConstructorParameters(Element element, BeanDefinitionBuilder actionBuilder) {
        for (String parameterName : constructorParameters) {
            if (element.hasAttribute(parameterName)) {
                actionBuilder.addConstructorArgValue(element.getAttribute(parameterName));
            } else {
                List<String> values = collectChildNodeContents(element, parameterName);
                actionBuilder.addConstructorArgValue(values);
            }
        }
    }

    /**
     * Reads non-constructor parameters from the XML element and adds them as properties to the
     * provided {@link BeanDefinitionBuilder}.
     */
    private void readNonConstructorParameters(Element element,
        BeanDefinitionBuilder actionBuilder) {
        for (String parameterName : nonConstructorParameters) {
            if (isHandledBySuper(parameterName)) {
                continue;
            }

            // For java parameter types other that string, we need to use the non-typed java property
            // with type string. These properties in java dsl are identified by a trailing '$'. In xml
            // however, the trailing '$' is omitted. Thus, the respective names are prepared accordingly.
            String attributeName = parameterName;
            if (parameterName.endsWith("$")) {
                attributeName = parameterName.substring(0, parameterName.length() - 1);
            }

            Attr attribute = element.getAttributeNode(attributeName);
            if (attribute != null) {
                actionBuilder.addPropertyValue(
                    mapXmlAttributeNameToJavaPropertyName(parameterName),
                    attribute.getValue());
            } else {
                List<String> values = collectChildNodeContents(element, attributeName);
                if (values != null && !values.isEmpty()) {
                    actionBuilder.addPropertyValue(
                        mapXmlAttributeNameToJavaPropertyName(parameterName),
                        values);
                }
            }
        }
    }

    /**
     * Sets the default endpoint for TestApi actions, if not already specified.
     */
    private void setDefaultEndpoint(BeanDefinitionBuilder beanDefinitionBuilder) {
        if (!beanDefinitionBuilder.getBeanDefinition().getPropertyValues().contains(ENDPOINT)
            && !beanDefinitionBuilder.getBeanDefinition().getPropertyValues()
            .contains(ENDPOINT_URI)) {
            beanDefinitionBuilder.addPropertyReference(ENDPOINT, defaultEndpointName);
        }
    }

    /**
     * Checks if the property is handled by the superclass implementation.
     *
     * @param property The property name to check.
     * @return True if handled by the superclass, false otherwise.
     */
    private boolean isHandledBySuper(String property) {
        return "body".equals(property);
    }

    @Override
    protected Class<? extends AbstractSendMessageActionFactoryBean<?, ?, ?>> getMessageFactoryClass() {
        return TestApiOpenApiClientSendActionBuilderFactoryBean.class;
    }

    @Override
    protected void validateEndpointConfiguration(Element element) {
        // skip validation, as we support endpoint injection
    }

    @Override
    protected Element getRequestElement(Element element) {
        return element;
    }

    @Override
    protected HttpMessageBuilder createMessageBuilder(HttpMessage httpMessage) {
        httpMessage.path(path);
        return new TestApiClientRequestMessageBuilder(httpMessage,
            new OpenApiSpecificationSource(openApiSpecification), operationId);
    }

    public void setConstructorParameters(String... constructorParameters) {
        this.constructorParameters =
            constructorParameters != null ? asList(constructorParameters)
                : emptyList();
    }

    public void setNonConstructorParameters(String... nonConstructorParameters) {
        this.nonConstructorParameters =
            nonConstructorParameters != null ? asList(nonConstructorParameters)
                : emptyList();
    }

    /**
     * Factory bean for creating {@link SendMessageAction} instances using the provided
     * {@link RestApiSendMessageActionBuilder}.
     */
    public static class TestApiOpenApiClientSendActionBuilderFactoryBean extends
        AbstractSendMessageActionFactoryBean<SendMessageAction, HttpMessageBuilderSupport, HttpClientRequestActionBuilder> {

        private RestApiSendMessageActionBuilder builder;

        public TestApiOpenApiClientSendActionBuilderFactoryBean(
            RestApiSendMessageActionBuilder builder) {
            this.builder = builder;
        }

        @Override
        public SendMessageAction getObject() {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return SendMessageAction.class;
        }

        @Override
        public HttpClientRequestActionBuilder getBuilder() {
            return builder;
        }

        public void setBuilder(RestApiSendMessageActionBuilder builder) {
            this.builder = builder;
        }
    }
}
