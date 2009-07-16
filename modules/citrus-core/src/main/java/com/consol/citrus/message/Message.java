package com.consol.citrus.message;

import java.util.Map;

/**
 * Common message interface defining header and content for messages.
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2007
 */
public interface Message {
    public Map<String, String> getHeader();

    public void setHeader(Map<String, String> header);

    public String getMessagePayload();

    public void setMessagePayload(String content);

    public void addHeaderElement(String name, String value);
}
