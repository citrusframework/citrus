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

/**
 * Message receiver handling Http reply responses.
 * 
 * @author Christoph Deppisch
 */
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
    
    /**
     * @see com.consol.citrus.message.AbstractReplyMessageReceiver#receiveSelected(java.lang.String)
     */
    @Override
    public Message<?> receiveSelected(String selector) {
        return buildMessage(super.receiveSelected(selector));
    }

    /**
     * Build the response message.
     * @param receivedMessage
     * @return
     */
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
