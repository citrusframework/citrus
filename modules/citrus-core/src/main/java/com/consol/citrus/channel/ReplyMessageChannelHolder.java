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

package com.consol.citrus.channel;

import org.springframework.integration.core.MessageChannel;

/**
 * Interface defines methods for reply message holders to return message
 * channels for synchronous communication.
 * 
 * @author Christoph Deppisch
 */
public interface ReplyMessageChannelHolder {
    /**
     * Get reply message channel with given correlation key.
     * @param correlationKey
     * @return
     */
    MessageChannel getReplyMessageChannel(String correlationKey);
    
    /**
     * Get reply message channel.
     * @return
     */
    MessageChannel getReplyMessageChannel();
}
