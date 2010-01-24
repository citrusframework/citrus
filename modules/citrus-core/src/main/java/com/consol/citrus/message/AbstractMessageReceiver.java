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

package com.consol.citrus.message;

import org.springframework.integration.core.Message;

/**
 * Abstract base class for message receiver implementations.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractMessageReceiver implements MessageReceiver {

    /** Receive timeout in ms */
    private long receiveTimeout = 5000L;
    
    /**
     * @see com.consol.citrus.message.MessageReceiver#receive()
     */
    public Message<?> receive() {
        return receive(receiveTimeout);
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receive(long)
     */
    public abstract Message<?> receive(long timeout);

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String)
     */
    public Message<?> receiveSelected(String selector) {
        return receiveSelected(selector, receiveTimeout);
    }

    /**
     * @see com.consol.citrus.message.MessageReceiver#receiveSelected(java.lang.String, long)
     */
    public abstract Message<?> receiveSelected(String selector, long timeout);

    /**
     * Setter for receive timeout.
     * @param receiveTimeout the receiveTimeout to set
     */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }
    
}
