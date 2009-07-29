package com.consol.citrus.ws;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import com.consol.citrus.message.MessageSender;
import com.consol.citrus.message.ReplyMessageHandler;

public class WebServiceMessageSender extends WebServiceGatewaySupport implements MessageSender {

    private ReplyMessageHandler replyMessageHandler;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(WebServiceMessageSender.class);
    
    public WebServiceMessageSender(WebServiceMessageFactory messageFactory) {
        super(messageFactory);
    }
    
    public void send(Message<?> message) {
        Assert.notNull(message, "Can not send empty message");
        
        log.info("Sending message to: " + getDefaultUri());

        if (log.isDebugEnabled()) {
            log.debug("Message to be sent:");
            log.debug(message.toString());
        }
        
        StringWriter responseWriter = new StringWriter();
        StreamResult result = new StreamResult(responseWriter);

        StreamSource source = new StreamSource(new StringReader(message.getPayload().toString()));
        getWebServiceTemplate().sendSourceAndReceiveToResult(source, result);

        responseWriter.flush();
        if(replyMessageHandler != null) {
            replyMessageHandler.onReplyMessage(MessageBuilder.withPayload(responseWriter.toString()).build());
        }
        try {
            responseWriter.close();
        } catch (IOException e) {
            log.error("Error while closing output stream", e);
        }
    }

    /**
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;
    }
}
