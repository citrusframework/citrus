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

package com.consol.citrus.jms;

import javax.jms.Destination;

import com.consol.citrus.message.MessageReceiver;

/**
 * Reply destination holder interface for getting reply destinations in synchronous
 * JMS communication.
 * 
 * {@link MessageReceiver} implementation receives synchronous messages and saves the 
 * reply destination. Reply message senders may ask for the saved reply destination in order to
 * provide proper reply message.
 * 
 * @author Christoph Deppisch
 */
public interface JmsReplyDestinationHolder {
    /**
     * Get the reply destination with a correlation key. Usually used in multi threaded and parallel
     * testing where destination holder has to manage several destinations at a time.
     * 
     * Destinations are saves and loaded using a correlation key.  
     * @param correlationKey
     * @return
     */
    Destination getReplyDestination(String correlationKey);
    
    /**
     * Get the next reply destination. Used in single threaded testing where only one
     * destination is managed by the destination holder at a time.
     * @return
     */
    Destination getReplyDestination();
}
