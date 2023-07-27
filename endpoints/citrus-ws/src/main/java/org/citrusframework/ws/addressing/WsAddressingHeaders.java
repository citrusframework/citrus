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

package org.citrusframework.ws.addressing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.springframework.ws.soap.addressing.core.EndpointReference;

/**
 * Value object holding ws addressing information which is translated into the message header.
 * Message id is optional - if not set here it is generated automatically.
 *
 * @author Christoph Deppisch
 */
public class WsAddressingHeaders {
    /** Addressing headers version */
    private WsAddressingVersion version;

    /** List of defined must understand headers */
    private List<String> mustUnderstandHeaders = new ArrayList<>();

    /** Action */
    private URI action;

    /** Endpoint destination */
    private URI to;

    /** The unique message id */
    private URI messageId;

    /** Endpoint reference from */
    private EndpointReference from;

    /** ReplyTo endpoint reference */
    private EndpointReference replyTo;

    /** Fault endpoint reference */
    private EndpointReference faultTo;

    /**
     * Gets the adressing version.
     * @return the version
     */
    public WsAddressingVersion getVersion() {
        return version;
    }

    /**
     * Sets the adressing version.
     * @param version the version to set
     */
    public void setVersion(WsAddressingVersion version) {
        this.version = version;
    }

    /**
     * Gets the action.
     * @return the action
     */
    public URI getAction() {
        return action;
    }

    /**
     * Sets the action.
     * @param action the action to set
     */
    public void setAction(URI action) {
        this.action = action;
    }

    /**
     * Sets the action from uri string.
     * @param action the action to set
     */
    public void setAction(String action) {
        try {
            this.action = new URI(action);
        } catch (URISyntaxException e) {
            throw new CitrusRuntimeException("Invalid action uri", e);
        }
    }

    /**
     * Gets the to uri.
     * @return the to
     */
    public URI getTo() {
        return to;
    }

    /**
     * Sets the to uri.
     * @param to the to to set
     */
    public void setTo(URI to) {
        this.to = to;
    }

    /**
     * Sets the to uri by string.
     * @param to the to to set
     */
    public void setTo(String to) {
        try {
            this.to = new URI(to);
        } catch (URISyntaxException e) {
            throw new CitrusRuntimeException("Invalid to uri", e);
        }
    }

    /**
     * Gets the message id.
     * @return the messageId
     */
    public URI getMessageId() {
        return messageId;
    }

    /**
     * Sets the message id.
     * @param messageId the messageId to set
     */
    public void setMessageId(URI messageId) {
        this.messageId = messageId;
    }

    /**
     * Sets the message id from uri string.
     * @param messageId the messageId to set
     */
    public void setMessageId(String messageId) {
        try {
            this.messageId = new URI(messageId);
        } catch (URISyntaxException e) {
            throw new CitrusRuntimeException("Invalid messageId uri", e);
        }
    }

    /**
     * Gets the from endpoint reference.
     * @return the from
     */
    public EndpointReference getFrom() {
        return from;
    }

    /**
     * Sets the from endpoint reference.
     * @param from the from to set
     */
    public void setFrom(EndpointReference from) {
        this.from = from;
    }

    /**
     * Sets the from endpoint reference by string.
     * @param from the from to set
     */
    public void setFrom(String from) {
        try {
            this.from = new EndpointReference(new URI(from));
        } catch (URISyntaxException e) {
            throw new CitrusRuntimeException("Invalid from uri", e);
        }
    }

    /**
     * Gets the reply to endpoint reference.
     * @return the replyTo
     */
    public EndpointReference getReplyTo() {
        return replyTo;
    }

    /**
     * Sets the reply to endpoint reference.
     * @param replyTo the replyTo to set
     */
    public void setReplyTo(EndpointReference replyTo) {
        this.replyTo = replyTo;
    }

    /**
     * Sets the reply to endpoint reference by string.
     * @param replyTo the replyTo to set
     */
    public void setReplyTo(String replyTo) {
        try {
            this.replyTo = new EndpointReference(new URI(replyTo));
        } catch (URISyntaxException e) {
            throw new CitrusRuntimeException("Invalid replyTo uri", e);
        }
    }

    /**
     * Gets the fault to endpoint reference.
     * @return the faultTo
     */
    public EndpointReference getFaultTo() {
        return faultTo;
    }

    /**
     * Sets the fault to endpoint reference.
     * @param faultTo the faultTo to set
     */
    public void setFaultTo(EndpointReference faultTo) {
        this.faultTo = faultTo;
    }

    /**
     * Sets the fault to endpoint reference by string.
     * @param faultTo the faultTo to set
     */
    public void setFaultTo(String faultTo) {
        try {
            this.faultTo = new EndpointReference(new URI(faultTo));
        } catch (URISyntaxException e) {
            throw new CitrusRuntimeException("Invalid faultTo uri", e);
        }
    }

    /**
     * Gets the list of specified must understand headers.
     * @return
     */
    public List<String> getMustUnderstandHeaders() {
        return mustUnderstandHeaders;
    }

    /**
     * Sets the list of defined must understand headers.
     * @param mustUnderstandHeaders
     */
    public void setMustUnderstandHeaders(List<String> mustUnderstandHeaders) {
        this.mustUnderstandHeaders = mustUnderstandHeaders;
    }

    /**
     * Determines if given header needs to set must understand flag.
     * @param header
     * @return
     */
    public boolean isMustUnderstand(QName header) {
        return this.mustUnderstandHeaders.contains(header.toString()) || this.mustUnderstandHeaders.contains(header.getPrefix() + ":" + header.getLocalPart());
    }

    /**
     * Determines if this addressing headers instance has defined must understand headers.
     * @return true, when at least one must understand header is defined.
     */
    public boolean hasMustUnderstandHeaders() {
        return !this.mustUnderstandHeaders.isEmpty();
    }
}
