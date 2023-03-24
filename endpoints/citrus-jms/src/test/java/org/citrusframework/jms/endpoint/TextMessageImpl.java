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

package org.citrusframework.jms.endpoint;

import jakarta.jms.*;
import java.util.*;

/**
 * @author Christoph Deppisch
 */
public class TextMessageImpl implements TextMessage {
    private String payload;
    
    private Destination replyDestination = null;
    
    private Map<String, Object> headers;
    
    public TextMessageImpl(String payload, Map<String, Object> headers) {
        this.payload = payload;
        this.headers = headers;
    }
    
    public void setStringProperty(String name, String value) throws JMSException {headers.put(name, value);}
    public void setShortProperty(String name, short value) throws JMSException {}
    public void setObjectProperty(String name, Object value) throws JMSException {}
    public void setLongProperty(String name, long value) throws JMSException {}
    public void setJMSType(String type) throws JMSException {}
    public void setJMSTimestamp(long timestamp) throws JMSException {}
    public void setJMSReplyTo(Destination replyTo) throws JMSException {this.replyDestination=replyTo;}
    public void setJMSRedelivered(boolean redelivered) throws JMSException {}
    public void setJMSPriority(int priority) throws JMSException {}
    public void setJMSMessageID(String id) throws JMSException {}
    public void setJMSExpiration(long expiration) throws JMSException {}
    public long getJMSDeliveryTime() throws JMSException { return 0; }
    public void setJMSDeliveryTime(long deliveryTime) throws JMSException {}
    public void setJMSDestination(Destination destination) throws JMSException {}
    public void setJMSDeliveryMode(int deliveryMode) throws JMSException {}
    public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {}
    public void setJMSCorrelationID(String correlationID) throws JMSException {}
    public void setIntProperty(String name, int value) throws JMSException {}
    public void setFloatProperty(String name, float value) throws JMSException {}
    public void setDoubleProperty(String name, double value) throws JMSException {}
    public void setByteProperty(String name, byte value) throws JMSException {}
    public void setBooleanProperty(String name, boolean value) throws JMSException {}
    public boolean propertyExists(String name) throws JMSException {return false;}
    public String getStringProperty(String name) throws JMSException {return headers.get(name).toString();}
    public short getShortProperty(String name) throws JMSException {return 0;}
    @SuppressWarnings("rawtypes")
    public Enumeration getPropertyNames() throws JMSException {return new Vector<String>(headers.keySet()).elements();}
    public Object getObjectProperty(String name) throws JMSException {return headers.get(name);}
    public long getLongProperty(String name) throws JMSException {return 0;}
    public String getJMSType() throws JMSException {return null;}
    public long getJMSTimestamp() throws JMSException {return 0;}
    public Destination getJMSReplyTo() throws JMSException {return replyDestination;}
    public boolean getJMSRedelivered() throws JMSException {return false;}
    public int getJMSPriority() throws JMSException {return 0;}
    public String getJMSMessageID() throws JMSException {return "123456789";}
    public long getJMSExpiration() throws JMSException {return 0;}
    public Destination getJMSDestination() throws JMSException {return null;}
    public int getJMSDeliveryMode() throws JMSException {return 0;}
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {return null;}
    public String getJMSCorrelationID() throws JMSException {return null;}
    public int getIntProperty(String name) throws JMSException {return 0;}
    public float getFloatProperty(String name) throws JMSException {return 0;}
    public double getDoubleProperty(String name) throws JMSException {return 0;}
    public byte getByteProperty(String name) throws JMSException {return 0;}
    public boolean getBooleanProperty(String name) throws JMSException {return false;}
    public void clearProperties() throws JMSException {}
    public void clearBody() throws JMSException {}
    public <T> T getBody(Class<T> c) throws JMSException { return (T) payload; }
    public boolean isBodyAssignableTo(Class c) throws JMSException { return true; }
    public void acknowledge() throws JMSException {}
    public void setText(String string) throws JMSException {this.payload = string;}
    public String getText() throws JMSException {return payload;}
}
