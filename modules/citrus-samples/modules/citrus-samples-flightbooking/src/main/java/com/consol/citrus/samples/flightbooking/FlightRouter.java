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

package com.consol.citrus.samples.flightbooking;

import java.util.Collection;
import java.util.Collections;

import org.springframework.integration.annotation.Router;
import org.springframework.integration.channel.ChannelResolver;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;

import com.consol.citrus.samples.flightbooking.model.FlightBookingRequestMessage;

/**
 * @author Christoph Deppisch
 */
public class FlightRouter {
    
    private ChannelResolver channelResolver;
    
    @Router
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
