package com.consol.citrus.jms;

import javax.jms.Destination;

import org.springframework.integration.core.Message;
import org.springframework.integration.jms.JmsHeaders;


public class JmsSyncMessageReceiver extends JmsMessageReceiver implements JmsReplyDestinationHolder {
    
    private Destination replyDestination;
    
    @Override
    public Message<?> receive(long timeout) {
        Message receivedMessage = super.receive(timeout);
        
        replyDestination = (Destination)receivedMessage.getHeaders().get(JmsHeaders.REPLY_TO);
        
        return receivedMessage;
    }
    
    @Override
    public Message<?> receiveSelected(String selector, long timeout) {
        Message receivedMessage = super.receiveSelected(selector, timeout);
        
        replyDestination = (Destination)receivedMessage.getHeaders().get(JmsHeaders.REPLY_TO);
        
        return receivedMessage;
    }

    public Destination getReplyDestination() {
        return replyDestination;
    }
}
