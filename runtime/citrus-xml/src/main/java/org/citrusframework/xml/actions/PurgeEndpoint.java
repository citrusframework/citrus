/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.xml.actions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.PurgeEndpointAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "purge-endpoint")
public class PurgeEndpoint implements TestActionBuilder<PurgeEndpointAction>, ReferenceResolverAware {

    private final PurgeEndpointAction.Builder builder = new PurgeEndpointAction.Builder();

    private ReferenceResolver referenceResolver;

    @XmlElement(name = "endpoint")
    protected List<Endpoint> endpoints;

    @XmlAttribute
    public PurgeEndpoint setEndpoint(String endpoint) {
        builder.endpoint(endpoint);
        return this;
    }

    public List<Endpoint> getEndpoints() {
        if (endpoints == null) {
            endpoints = new ArrayList<>();
        }
        return this.endpoints;
    }

    @XmlAttribute
    public PurgeEndpoint setTimeout(long milliseconds) {
        builder.timeout(milliseconds);
        return this;
    }

    @XmlAttribute
    public PurgeEndpoint setSleep(long milliseconds) {
        builder.sleep(milliseconds);
        return this;
    }

    @XmlAttribute
    public PurgeEndpoint setSelect(String value) {
        builder.selector(value);
        return this;
    }

    @XmlElement
    public PurgeEndpoint setSelector(Selector selector) {
        if (selector.selectorValue != null) {
            builder.selector(selector.selectorValue);
        }

        if (selector.elements != null) {
            Map<String, Object> selectorElements = new HashMap<>();
            for (Selector.Element element : selector.elements) {
                selectorElements.put(element.name, element.value);
            }

            builder.selector(selectorElements);
        }

        return this;
    }

    @Override
    public PurgeEndpointAction build() {
        builder.withReferenceResolver(referenceResolver);
        getEndpoints().forEach(endpoint -> builder.endpoint(endpoint.name));
        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Endpoint {

        @XmlAttribute(name = "name", required = true)
        protected String name;

        public String getName() {
            return name;
        }
        public void setName(String value) {
            this.name = value;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "elements",
            "selectorValue"
    })
    public static class Selector {
        @XmlElement(name = "element")
        protected List<Selector.Element> elements;
        @XmlElement(name = "value")
        protected String selectorValue;

        public List<Selector.Element> getElements() {
            if (elements == null) {
                elements = new ArrayList<>();
            }
            return this.elements;
        }

        public String getSelectorValue() {
            return selectorValue;
        }

        public void setSelectorValue(String value) {
            this.selectorValue = value;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Element {
            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute(name = "value", required = true)
            protected String value;

            public String getName() {
                return name;
            }

            public void setName(String value) {
                this.name = value;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }
}
