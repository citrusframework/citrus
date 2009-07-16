package com.consol.citrus.samples.flightbooking;

import java.util.Collection;
import java.util.Collections;

import org.springframework.integration.channel.ChannelResolver;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.router.AbstractMessageRouter;

import com.consol.citrus.samples.flightbooking.model.FlightBookingRequestMessage;

public class FlightRouter extends AbstractMessageRouter {
    
    ChannelResolver channelResolver;
    
    @Override
    public Collection<MessageChannel> determineTargetChannels(Message<?> message) {
        
        FlightBookingRequestMessage request = (FlightBookingRequestMessage)message.getPayload();
        
        return Collections.singletonList(channelResolver.resolveChannelName(request.getFlight().getAirline()));
    }

    /**
     * @return the channelResolver
     */
    public ChannelResolver getChannelResolver() {
        return channelResolver;
    }

    /**
     * @param channelResolver the channelResolver to set
     */
    public void setChannelResolver(ChannelResolver channelResolver) {
        this.channelResolver = channelResolver;
    }
}
