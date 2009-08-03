package com.consol.citrus.http.handler;

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

import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.message.MessageHandler;
import com.consol.citrus.util.XMLUtils;

public class XpathDispatchingMessageHandler implements MessageHandler {
    private String xpathMappingExpression;

    private String messageHandlerContext;
    
    public Message handleMessage(Message request) throws TestSuiteException {
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
                throw new TestSuiteException("Could not find matching element '" + xpathMappingExpression + "' in message");
            }

            //TODO support FileSystemContext
            ApplicationContext ctx = new ClassPathXmlApplicationContext(messageHandlerContext);
            MessageHandler handler = (MessageHandler)ctx.getBean(matchingElement.getNodeName(), MessageHandler.class);

            if (handler != null) {
                return handler.handleMessage(request);
            } else {
                throw new TestSuiteException("Could not find message handler with name '" + matchingElement.getNodeName() + "' in '" + messageHandlerContext + "'");
            }
        } catch (SAXException e) {
            throw new TestSuiteException(e);
        } catch (IOException e) {
            throw new TestSuiteException(e);
        }
    }
    
    public void setXpathMappingExpression(String mappingExpression) {
        this.xpathMappingExpression = mappingExpression;
    }

    public void setMessageHandlerContext(String messageHandlerContext) {
        this.messageHandlerContext = messageHandlerContext;
    }
}
