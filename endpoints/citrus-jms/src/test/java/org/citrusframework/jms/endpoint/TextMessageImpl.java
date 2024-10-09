/*
 * Copyright the original author or authors.
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

package org.citrusframework.jms.endpoint;

import jakarta.jms.Destination;
import jakarta.jms.TextMessage;

import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

public class TextMessageImpl implements TextMessage {
    private String payload;

    private Destination replyDestination = null;

    private final Map<String, Object> headers;

    public TextMessageImpl(String payload, Map<String, Object> headers) {
        this.payload = payload;
        this.headers = headers;
    }

    public void setStringProperty(String name, String value) {
        headers.put(name, value);
    }

    public void setShortProperty(String name, short value) {
    }

    public void setObjectProperty(String name, Object value) {
    }

    public void setLongProperty(String name, long value) {
    }

    public long getJMSDeliveryTime() {
        return 0;
    }

    public void setJMSDeliveryTime(long deliveryTime) {
    }

    public void setIntProperty(String name, int value) {
    }

    public void setFloatProperty(String name, float value) {
    }

    public void setDoubleProperty(String name, double value) {
    }

    public void setByteProperty(String name, byte value) {
    }

    public void setBooleanProperty(String name, boolean value) {
    }

    public boolean propertyExists(String name) {
        return false;
    }

    public String getStringProperty(String name) {
        return headers.get(name).toString();
    }

    public short getShortProperty(String name) {
        return 0;
    }

    @SuppressWarnings("rawtypes")
    public Enumeration getPropertyNames() {
        return new Vector<>(headers.keySet()).elements();
    }

    public Object getObjectProperty(String name) {
        return headers.get(name);
    }

    public long getLongProperty(String name) {
        return 0;
    }

    public String getJMSType() {
        return null;
    }

    public void setJMSType(String type) {
    }

    public long getJMSTimestamp() {
        return 0;
    }

    public void setJMSTimestamp(long timestamp) {
    }

    public Destination getJMSReplyTo() {
        return replyDestination;
    }

    public void setJMSReplyTo(Destination replyTo) {
        this.replyDestination = replyTo;
    }

    public boolean getJMSRedelivered() {
        return false;
    }

    public void setJMSRedelivered(boolean redelivered) {
    }

    public int getJMSPriority() {
        return 0;
    }

    public void setJMSPriority(int priority) {
    }

    public String getJMSMessageID() {
        return "123456789";
    }

    public void setJMSMessageID(String id) {
    }

    public long getJMSExpiration() {
        return 0;
    }

    public void setJMSExpiration(long expiration) {
    }

    public Destination getJMSDestination() {
        return null;
    }

    public void setJMSDestination(Destination destination) {
    }

    public int getJMSDeliveryMode() {
        return 0;
    }

    public void setJMSDeliveryMode(int deliveryMode) {
    }

    public byte[] getJMSCorrelationIDAsBytes() {
        return null;
    }

    public void setJMSCorrelationIDAsBytes(byte[] correlationID) {
    }

    public String getJMSCorrelationID() {
        return null;
    }

    public void setJMSCorrelationID(String correlationID) {
    }

    public int getIntProperty(String name) {
        return 0;
    }

    public float getFloatProperty(String name) {
        return 0;
    }

    public double getDoubleProperty(String name) {
        return 0;
    }

    public byte getByteProperty(String name) {
        return 0;
    }

    public boolean getBooleanProperty(String name) {
        return false;
    }

    public void clearProperties() {
    }

    public void clearBody() {
    }

    public <T> T getBody(Class<T> c) {
        return (T) payload;
    }

    public boolean isBodyAssignableTo(Class c) {
        return true;
    }

    public void acknowledge() {
    }

    public String getText() {
        return payload;
    }

    public void setText(String string) {
        this.payload = string;
    }
}
