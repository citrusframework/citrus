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

package com.consol.citrus.adapter.handler;

import com.consol.citrus.adapter.handler.mapping.SpringContextLoadingMessageHandlerMapping;
import com.consol.citrus.adapter.handler.mapping.XPathPayloadMappingKeyExtractor;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.Map;

/**
 * This message handler implementation dispatches incoming request to other message handlers
 * according to a XPath expression evaluated on the message payload of the incoming request.
 *
 * The XPath expression's result value will determine the message handler delegate. You can think of
 * having a message handler for each root element name, meaning the message type.
 *
 * All available message handlers are hosted in a separate Spring application context. The message handler
 * will search for a appropriate bean instance in this context according to the mapping expression.
 *
 * @author Christoph Deppisch
 * @deprecated since 1.3.1 in favour of RequestDispatchingMessageHandler with use of XPathMappingKeyExtractor and SpringBeanMessageHandlerMapping
 */
@Deprecated
public class XpathDispatchingMessageHandler extends RequestDispatchingMessageHandler implements InitializingBean {
    /** Dispatching XPath expression */
    private String xpathMappingExpression = "local-name(/*)";

    /** Application context holding available message handlers */
    protected String messageHandlerContext;

    /** Map holding namespace bindings for XPath expression */
    private Map<String, String> namespaceBindings = new HashMap<String, String>();

    @Override
    public void afterPropertiesSet() throws Exception {
        SpringContextLoadingMessageHandlerMapping handlerMapping = new SpringContextLoadingMessageHandlerMapping();
        handlerMapping.setContextConfigLocation(messageHandlerContext);

        setMessageHandlerMapping(handlerMapping);

        XPathPayloadMappingKeyExtractor mappingNameExtracor = new XPathPayloadMappingKeyExtractor();
        NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
        namespaceContextBuilder.setNamespaceMappings(namespaceBindings);
        mappingNameExtracor.setNamespaceContextBuilder(namespaceContextBuilder);
        mappingNameExtracor.setXpathExpression(xpathMappingExpression);

        setMappingKeyExtractor(mappingNameExtracor);
    }

    /**
     * Set the XPath mapping expression.
     * @param mappingExpression
     */
    public void setXpathMappingExpression(String mappingExpression) {
        this.xpathMappingExpression = mappingExpression;
    }

    /**
     * Set the message handler context.
     * @param messageHandlerContext
     */
    public void setMessageHandlerContext(String messageHandlerContext) {
        this.messageHandlerContext = messageHandlerContext;
    }

    /**
     * Set the namespace bindings for XPath expression evaluation.
     * @param namespaceBindings the namespaceBindings to set
     */
    public void setNamespaceBindings(Map<String, String> namespaceBindings) {
        this.namespaceBindings = namespaceBindings;
    }
}
