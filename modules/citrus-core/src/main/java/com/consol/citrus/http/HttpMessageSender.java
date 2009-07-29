package com.consol.citrus.http;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;

import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.message.ReplyMessageHandler;
import com.consol.citrus.util.MessageUtils;

public class HttpMessageSender implements MessageSender {
    /** Http url as service detination */
    private String urlPath;

    /** Http host */
    private String host;

    /** Port */
    private int port;

    /** Request method */
    private String requestMethod = HttpConstants.HTTP_POST;

    /** Http socket */
    private Socket socket;
    
    private ReplyMessageHandler replyMessageHandler;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(HttpMessageSender.class);
    
    public void send(Message<?> message) {
        Writer writer = null;
        BufferedReader reader = null;

        try {
            log.info("Sending message to: " + getDestinationUri());

            if (log.isDebugEnabled()) {
                log.debug("Message to be sent:");
                log.debug(message.getPayload().toString());
            }

            Map<String, Object> requestHeaders = new HashMap<String, Object>();
            
            requestHeaders.put("HTTPVersion", HttpConstants.HTTP_VERSION);
            requestHeaders.put("HTTPMethod", requestMethod);
            requestHeaders.put("HTTPUri", urlPath);
            requestHeaders.put("HTTPHost", host);
            requestHeaders.put("HTTPPort", Integer.valueOf(port).toString());

            /* before sending set header values */
            for (Entry headerEntry : message.getHeaders().entrySet()) {
                final String key = headerEntry.getKey().toString();
                
                if(MessageUtils.isSpringIntegrationHeaderEntry(key)) {
                    continue;
                }
                
                final String value = (String) headerEntry.getValue();

                if (log.isDebugEnabled()) {
                    log.debug("Setting message property: " + key + " to: " + value);
                }

                requestHeaders.put(key, value);
            }

            Message request;
            if (requestMethod.equals(HttpConstants.HTTP_POST)) {
                request = MessageBuilder.withPayload(message.getPayload()).copyHeaders(requestHeaders).build();
            } else if (requestMethod.equals(HttpConstants.HTTP_GET)) {
                //TODO: implement GET method
                request = MessageBuilder.withPayload("").build();
            } else {
                throw new TestSuiteException("Unsupported request method: " + requestMethod);
            }

            InetAddress addr = InetAddress.getByName(host);
            socket = new Socket(addr, port);

            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF8"));
            writer.write(HttpUtils.generateRequest(request));
            writer.flush();

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append(HttpConstants.LINE_BREAK);
            }

            if(replyMessageHandler != null) {
                replyMessageHandler.onReplyMessage(MessageBuilder.withPayload(buffer.toString()).build());
            }
        } catch (IOException e) {
            throw new TestSuiteException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.error("Error while closing OutputStream", e);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error while closing InputStream", e);
                }
            }
        }
    }

    private String getDestinationUri() {
        return "http://" + host + ":" + port + urlPath;
    }

    /**
     * @return the urlPath
     */
    public String getUrlPath() {
        return urlPath;
    }

    /**
     * @param urlPath the urlPath to set
     */
    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    /**
     * @return the socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * @param socket the socket to set
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @param requestMethod the requestMethod to set
     */
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;
    }
}
