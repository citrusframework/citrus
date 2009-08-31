package com.consol.citrus.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.channel.interceptor.ChannelInterceptorAdapter;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;

public class LoggingInterceptor extends ChannelInterceptorAdapter {
    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        log.info(channel.getName() + ": " + message.getPayload());
        
        if(message.getPayload() instanceof Throwable) {
            ((Throwable)message.getPayload()).printStackTrace();
        }
        
        return super.preSend(message, channel);
    }
}
