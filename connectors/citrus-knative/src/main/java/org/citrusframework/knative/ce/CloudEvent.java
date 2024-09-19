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

import java.util.Arrays;
import java.util.List;

public class CloudEvent {

    private final String version;
    private final List<Attribute> attributes;

    private CloudEvent(String version, List<Attribute> attributes) {
        this.version = version;
        this.attributes = attributes;
    }

    public String version() {
        return version;
    }

    public List<Attribute> attributes() {
        return attributes;
    }

    /**
     * Create new cloud event for version 1.0
     * https://github.com/cloudevents/spec/blob/v1.0/spec.md
     * @return
     */
    public static CloudEvent v1_0() {
        return new CloudEvent(
                "1.0",
                Arrays.asList(
                        Attribute.ID,
                        Attribute.SOURCE,
                        Attribute.SPEC_VERSION,
                        Attribute.TYPE,
                        Attribute.SUBJECT,
                        Attribute.DATA_SCHEMA,
                        Attribute.TIME,
                        Attribute.CONTENT_TYPE
                )
        );
    }

    /**
     * Cloud event attribute with Http header name and Json field name representation. Optional default value
     * can be specified.
     */
    public enum Attribute {

        ID("Ce-Id", "id"),
        SOURCE("Ce-Source", "source"),
        SPEC_VERSION("Ce-Specversion", "specversion", "1.0"),
        TYPE("Ce-Type", "type"),
        SUBJECT("Ce-Subject", "subject"),
        DATA_SCHEMA("Ce-Dataschema", "dataschema"),
        TIME("Ce-Time", "time"),
        CONTENT_TYPE("Content-Type", "datacontenttype");

        private final String http;
        private final String json;
        private final String defaultValue;

        /**
         * The name of the http header.
         */
        public String http() {
            return this.http;
        }

        /**
         * The name of the json field.
         */
        public String json() {
            return this.json;
        }

        /**
         * Default value if any.
         */
        public String defaultValue() {
            return this.defaultValue;
        }

        /**
         * Checks if this attribute provides a default value.
         * @return
         */
        public boolean hasDefaultValue() {
            return defaultValue != null;
        }

        Attribute(String http, String json) {
            this(http, json, null);
        }

        Attribute(String http, String json, String defaultValue) {
            this.http = http;
            this.json = json;
            this.defaultValue = defaultValue;
        }
    }
}
