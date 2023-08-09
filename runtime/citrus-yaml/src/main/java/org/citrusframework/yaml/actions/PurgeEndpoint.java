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

package org.citrusframework.yaml.actions;

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
public class PurgeEndpoint implements TestActionBuilder<PurgeEndpointAction>, ReferenceResolverAware {

    private final PurgeEndpointAction.Builder builder = new PurgeEndpointAction.Builder();

    private ReferenceResolver referenceResolver;

    protected List<Endpoint> endpoints;

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

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public void setTimeout(long milliseconds) {
        builder.timeout(milliseconds);
    }

    public void setSleep(long milliseconds) {
        builder.sleep(milliseconds);
    }

    public void setSelect(String value) {
        builder.selector(value);
    }

    public void setSelector(Selector selector) {
        if (selector.value != null) {
            builder.selector(selector.value);
        }

        if (selector.elements != null) {
            Map<String, Object> selectorElements = new HashMap<>();
            for (Selector.Element element : selector.elements) {
                selectorElements.put(element.name, element.value);
            }

            builder.selector(selectorElements);
        }
    }

    @Override
    public PurgeEndpointAction build() {
        builder.withReferenceResolver(referenceResolver);

        for (Endpoint endpoint : endpoints) {
            if (endpoint.getName() != null) {
                builder.endpoint(endpoint.name);
            }

            if (referenceResolver != null && endpoint.getRef() != null) {
                builder.endpoint(referenceResolver.resolve(endpoint.ref, org.citrusframework.endpoint.Endpoint.class));
            }
        }
        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    public static class Endpoint {

        protected String name;
        protected String ref;

        public String getName() {
            return name;
        }
        public void setName(String value) {
            this.name = value;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }

        public String getRef() {
            return ref;
        }
    }

    public static class Selector {
        protected List<Element> elements;
        protected String value;

        public List<Element> getElements() {
            if (elements == null) {
                elements = new ArrayList<>();
            }
            return this.elements;
        }

        public void setElements(List<Element> elements) {
            this.elements = elements;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public static class Element {
            protected String name;
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
