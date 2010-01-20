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

package com.consol.citrus.http.message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.util.HttpConstants;
import com.consol.citrus.message.AbstractReplyMessageReceiver;

public class HttpReplyMessageReceiver extends AbstractReplyMessageReceiver {
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(HttpReplyMessageReceiver.class);
    
    /**
     * @see com.consol.citrus.message.AbstractReplyMessageReceiver#receive()
     * @throws CitrusRuntimeException
     */
    @Override
    public Message<?> receive() {
        return buildMessage(super.receive());
    }
    
    @Override
    public Message<?> receiveSelected(String selector) {
        return buildMessage(super.receiveSelected(selector));
    }

    private Message<?> buildMessage(Message<?> receivedMessage) {
        Message<?> httpResponse;
        try {
            BufferedReader reader = new BufferedReader(new StringReader(receivedMessage.getPayload().toString()));
            
            String readLine = null;
            readLine = reader.readLine();

            if (readLine == null || readLine.length() == 0) {
                throw new RuntimeException("HTTP response header not set properly. Usage: <HTTP VERSION> <STATUS CODE> <STATUS> ");
            }

            Map<String, Object> responseHeaders = new HashMap<String, Object>();
            StringTokenizer st = new StringTokenizer(readLine);
            if (!st.hasMoreTokens()) {
                throw new RuntimeException("HTTP response header not set properly. Usage: <HTTP VERSION> <STATUS CODE> <STATUS> ");
            } else {
                responseHeaders.put("HTTPVersion", st.nextToken().toUpperCase());
            }

            if (!st.hasMoreTokens()) {
                throw new RuntimeException("HTTP response header not set properly. Usage: <HTTP VERSION> <STATUS CODE> <STATUS> ");
            } else {
                responseHeaders.put("HTTPStatusCode", st.nextToken());
            }

            if (!st.hasMoreTokens()) {
                throw new RuntimeException("HTTP response header not set properly. Usage: <HTTP VERSION> <STATUS CODE> <STATUS> ");
            } else {
                responseHeaders.put("HTTPReasonPhrase", st.nextToken());
            }

            // Read the request headers
            String line;
            do {
                line = reader.readLine();

                if(line != null) {
                    int p = line.indexOf(':');
                    if (p > 0) {
                        responseHeaders.put(line.substring(0, p).trim().toLowerCase(), line.substring(p + 1).trim());
                    }
                }
            } while (line != null && line.trim().length() > 0);

            StringBuffer contentBuffer = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                contentBuffer.append(line).append(HttpConstants.LINE_BREAK);
            }

            httpResponse = MessageBuilder.withPayload(contentBuffer.toString()).copyHeaders(responseHeaders).build();
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Message received:");
            log.debug(httpResponse.toString());
        }
        
        return httpResponse;
    }
}
