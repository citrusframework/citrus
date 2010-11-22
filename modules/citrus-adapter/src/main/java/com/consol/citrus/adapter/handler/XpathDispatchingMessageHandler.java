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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.util.DOMUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.core.Message;
import org.springframework.util.*;
import org.springframework.xml.namespace.SimpleNamespaceContext;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageHandler;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.xml.xpath.XPathUtils;

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
 */
public class XpathDispatchingMessageHandler implements MessageHandler {
    /** Dispatching XPath expression */
    private String xpathMappingExpression;

    /** Application context holding available message handlers */
    private String messageHandlerContext;
    
    /** Map holding namespace bindings for XPath expression */
    private Map<String, String> namespaceBindings = new HashMap<String, String>();
    
    /**
     * @see com.consol.citrus.message.MessageHandler#handleMessage(org.springframework.integration.core.Message)
     * @throws CitrusRuntimeException
     */
    public Message<?> handleMessage(Message<?> request) {
        Assert.notNull(messageHandlerContext, "MessageHandler application context must not be empty or null");
        
        try {
            final Reader reader = new StringReader(request.getPayload().toString());
            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false);

            parser.parse(new InputSource(reader));

            Node matchingElement;
            if (xpathMappingExpression != null) {
                SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
                if(!CollectionUtils.isEmpty(namespaceBindings)) {
                    nsContext.setBindings(namespaceBindings);
                } else {
                    nsContext.setBindings(XMLUtils.lookupNamespaces(request.getPayload().toString()));
                }
                
                matchingElement = XPathUtils.evaluateAsNode(DOMUtil.getFirstChildElement(parser.getDocument()), xpathMappingExpression, nsContext);
            } else {
                matchingElement = DOMUtil.getFirstChildElement(parser.getDocument());
            }

            if (matchingElement == null) {
                throw new CitrusRuntimeException("Could not find matching element '" + xpathMappingExpression + "' in message");
            }

            //TODO support FileSystemContext
            ApplicationContext ctx = new ClassPathXmlApplicationContext(messageHandlerContext);
            MessageHandler handler = (MessageHandler)ctx.getBean(matchingElement.getNodeName(), MessageHandler.class);

            if (handler != null) {
                return handler.handleMessage(request);
            } else {
                throw new CitrusRuntimeException("Could not find message handler with name '" + matchingElement.getNodeName() + "' in '" + messageHandlerContext + "'");
            }
        } catch (SAXException e) {
            throw new CitrusRuntimeException(e);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
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
