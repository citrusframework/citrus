package com.consol.citrus.samples.flightbooking.util;

import org.springframework.integration.channel.interceptor.ChannelInterceptorAdapter;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;

public class LoggingInterceptor extends ChannelInterceptorAdapter {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        System.out.println(channel.getName() + ": " + message.getPayload());
        
        if(message.getPayload() instanceof Throwable) {
            ((Throwable)message.getPayload()).printStackTrace();
        }
        
        return super.preSend(message, channel);
    }
}
