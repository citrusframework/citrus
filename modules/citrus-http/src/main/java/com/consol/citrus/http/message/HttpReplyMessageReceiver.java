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

import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.http.util.HttpConstants;
import com.consol.citrus.message.ReplyMessageReceiver;

public class HttpReplyMessageReceiver extends ReplyMessageReceiver {
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(HttpReplyMessageReceiver.class);
    
    @Override
    public Message<?> receive() {
        if (log.isDebugEnabled()) {
            log.debug("Message received:");
            log.debug(getReplyMessage().toString());
        }

        Message httpResponse;
        try {
            BufferedReader reader = new BufferedReader(new StringReader(getReplyMessage().getPayload().toString()));

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
            throw new TestSuiteException(e);
        }

        return httpResponse;
    }
}
