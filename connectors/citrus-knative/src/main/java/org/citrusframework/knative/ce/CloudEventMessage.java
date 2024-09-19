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

package org.citrusframework.knative.ce;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.http.message.HttpMessage;

public class CloudEventMessage extends HttpMessage {

    private final Map<CloudEvent.Attribute, Object> attributes = new HashMap<>();

    public CloudEventMessage eventId(String id) {
        return setAttribute(CloudEvent.Attribute.ID, id);
    }

    public Object getEventId() {
        return getAttribute(CloudEvent.Attribute.ID);
    }

    public CloudEventMessage eventType(String type) {
        return setAttribute(CloudEvent.Attribute.TYPE, type);
    }

    public Object getEventType() {
        return getAttribute(CloudEvent.Attribute.TYPE);
    }

    public CloudEventMessage specVersion(String version) {
        return setAttribute(CloudEvent.Attribute.SPEC_VERSION, version);
    }

    public Object getSpecVersion() {
        return getAttribute(CloudEvent.Attribute.SPEC_VERSION);
    }

    public CloudEventMessage source(String source) {
        return setAttribute(CloudEvent.Attribute.SOURCE, source);
    }

    public Object getSource() {
        return getAttribute(CloudEvent.Attribute.SOURCE);
    }

    public CloudEventMessage subject(String subject) {
        return setAttribute(CloudEvent.Attribute.SUBJECT, subject);
    }

    public Object getSubject() {
        return getAttribute(CloudEvent.Attribute.SUBJECT);
    }

    public CloudEventMessage time(String time) {
        return setAttribute(CloudEvent.Attribute.TIME, time);
    }

    public Object getTime() {
        return getAttribute(CloudEvent.Attribute.TIME);
    }

    public CloudEventMessage dataSchema(String schema) {
        return setAttribute(CloudEvent.Attribute.DATA_SCHEMA, schema);
    }

    public Object getDataSchema() {
        return getAttribute(CloudEvent.Attribute.DATA_SCHEMA);
    }

    public Object getAttribute(CloudEvent.Attribute attribute) {
        return attributes.get(attribute);
    }

    public CloudEventMessage setAttribute(CloudEvent.Attribute attribute, Object value) {
        attributes.put(attribute, value);
        header(attribute.http(), value);
        return this;
    }

    public static CloudEventMessage fromEvent(CloudEvent event) {
        CloudEventMessage message = new CloudEventMessage();

        event.attributes().stream()
                .filter(CloudEvent.Attribute::hasDefaultValue)
                .forEach(a -> message.setAttribute(a, a.defaultValue()));

        return message;
    }
}
