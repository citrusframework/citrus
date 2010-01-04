/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.samples.flightbooking;

import java.util.Collection;
import java.util.Collections;

import org.springframework.integration.channel.ChannelResolver;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.router.AbstractMessageRouter;

import com.consol.citrus.samples.flightbooking.model.FlightBookingRequestMessage;

public class FlightRouter extends AbstractMessageRouter {
    
    private ChannelResolver channelResolver;
    
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
