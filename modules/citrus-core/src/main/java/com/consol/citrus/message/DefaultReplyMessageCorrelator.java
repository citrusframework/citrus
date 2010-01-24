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
import org.springframework.integration.core.MessageHeaders;

/**
 * Default message correlator implementation using the Spring integration message id
 * as correlation key.
 * 
 * @author Christoph Deppisch
 */
public class DefaultReplyMessageCorrelator implements ReplyMessageCorrelator {

    /**
     * @see com.consol.citrus.message.ReplyMessageCorrelator#getCorrelationKey(org.springframework.integration.core.Message)
     */
    public String getCorrelationKey(Message<?> request) {
        return MessageHeaders.ID + " = '" + request.getHeaders().getId().toString() + "'";
    }

    /**
     * @see com.consol.citrus.message.ReplyMessageCorrelator#getCorrelationKey(java.lang.String)
     */
    public String getCorrelationKey(String id) {
        return MessageHeaders.ID + " = '" + id + "'";
    }
}
