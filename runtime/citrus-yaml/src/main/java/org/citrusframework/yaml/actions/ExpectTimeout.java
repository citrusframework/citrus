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
import org.citrusframework.actions.ReceiveTimeoutAction;

/**
 * @author Christoph Deppisch
 */
public class ExpectTimeout implements TestActionBuilder<ReceiveTimeoutAction> {

    private final ReceiveTimeoutAction.Builder builder = new ReceiveTimeoutAction.Builder();

    public void setWait(long milliseconds) {
        builder.timeout(milliseconds);
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

    public void setEndpoint(String endpointUri) {
        builder.endpoint(endpointUri);
    }

    @Override
    public ReceiveTimeoutAction build() {
        return builder.build();
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
