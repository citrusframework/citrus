package com.consol.citrus.endpoint.adapter;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by puneet.khandelwal on 8/11/2016.
 */
public class FileResourceResponseEndpointAdapter extends StaticEndpointAdapter {
    /**
     * Response message payload
     */
    private String messagePayload = "";

    /**
     * Response message header
     */
    private Map<String, Object> messageHeader = new HashMap<String, Object>();

    @Autowired
    private TestContext context;

    /**
     * Response file resource path
     */
    private URI filePath;

    public FileResourceResponseEndpointAdapter(URI filePath) {
        this.filePath = filePath;
    }

    @Override
    public Message handleMessageInternal(Message message) {
        try {
            setMessagePayload(context.replaceDynamicContentInString(new String(Files.readAllBytes(Paths.get(filePath)))));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Unable to find file resource with name '" +
                    filePath + "' in Spring bean context", e);
        }
        return new DefaultMessage(messagePayload, messageHeader);
    }

    /**
     * Gets the message payload.
     *
     * @return
     */
    public String getMessagePayload() {
        return messagePayload;
    }

    /**
     * Set the response message payload.
     *
     * @param messagePayload the messagePayload to set
     */
    public void setMessagePayload(String messagePayload) {
        this.messagePayload = messagePayload;
    }

    /**
     * Gets the message header.
     *
     * @return
     */
    public Map<String, Object> getMessageHeader() {
        return messageHeader;
    }

    /**
     * Set the response message header.
     *
     * @param messageHeader the messageHeader to set
     */
    public void setMessageHeader(Map<String, Object> messageHeader) {
        this.messageHeader = messageHeader;
    }
}
