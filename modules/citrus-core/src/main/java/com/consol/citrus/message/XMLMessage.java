package com.consol.citrus.message;

import java.util.HashMap;
import java.util.Map;

public class XMLMessage implements Message {

    private Map messageHeader = new HashMap<String, String>();

    private String messagePayload = null;

    public Map<String, String> getHeader() {
        return messageHeader;
    }

    public String getMessagePayload() {
        return messagePayload;
    }

    public void setHeader(Map<String, String> header) {
        this.messageHeader = header;
    }

    public void setMessagePayload(String content) {
        this.messagePayload = content;
    }

    public void addHeaderElement(String name, String value) {
        messageHeader.put(name, value);
    }
}
