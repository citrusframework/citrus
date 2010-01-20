/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.adapter.handler;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.util.DOMUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.core.Message;
import org.springframework.util.Assert;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageHandler;
import com.consol.citrus.util.XMLUtils;

public class XpathDispatchingMessageHandler implements MessageHandler {
    private String xpathMappingExpression;

    private String messageHandlerContext;
    
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
                matchingElement = XMLUtils.findNodeByXPath(DOMUtil.getFirstChildElement(parser.getDocument()), xpathMappingExpression);
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
    
    public void setXpathMappingExpression(String mappingExpression) {
        this.xpathMappingExpression = mappingExpression;
    }

    public void setMessageHandlerContext(String messageHandlerContext) {
        this.messageHandlerContext = messageHandlerContext;
    }
}
