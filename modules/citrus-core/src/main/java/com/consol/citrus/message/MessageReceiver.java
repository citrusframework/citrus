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
 * Message receiver interface declares message receiving methods.
 * @author Christoph Deppisch
 */
public interface MessageReceiver {
    /**
     * Receive message.
     * @return
     */
    Message<?> receive();
    
    /**
     * Receive message with a given timeout.
     * @param timeout
     * @return
     */
    Message<?> receive(long timeout);
    
    /**
     * Receive message with a message selector string.
     * @param selector
     * @return
     */
    Message<?> receiveSelected(String selector);
    
    /**
     * Receive message with a message selector and a receive timeout.
     * @param selector
     * @param timeout
     * @return
     */
    Message<?> receiveSelected(String selector, long timeout);
}
