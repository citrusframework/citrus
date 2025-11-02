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

package org.citrusframework.yaml.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReceiveTimeoutAction;
import org.citrusframework.yaml.SchemaProperty;

public class ExpectTimeout implements TestActionBuilder<ReceiveTimeoutAction> {

    private final ReceiveTimeoutAction.Builder builder = new ReceiveTimeoutAction.Builder();

    @SchemaProperty(description = "Time in milliseconds to wait for messages.")
    public void setWait(long milliseconds) {
        builder.timeout(milliseconds);
    }

    @SchemaProperty(advanced = true, description = "Optional message selector expression to selectively consume messages.")
    public void setSelect(String value) {
        builder.selector(value);
    }

    @SchemaProperty(advanced = true, description = "Message selector to selectively consume messages.")
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

    @SchemaProperty(required = true, description = "The message endpoint to consume messages from.")
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

        @SchemaProperty(advanced = true, description = "Selector elements building the selector expression.")
        public void setElements(List<Element> elements) {
            this.elements = elements;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(description = "Selector expression value.")
        public void setValue(String value) {
            this.value = value;
        }

        public static class Element {
            protected String name;
            protected String value;

            public String getName() {
                return name;
            }

            @SchemaProperty(required = true, description = "Selector key name.")
            public void setName(String value) {
                this.name = value;
            }

            public String getValue() {
                return value;
            }

            @SchemaProperty(required = true, description = "Selector expression value that the expression must match.")
            public void setValue(String value) {
                this.value = value;
            }
        }
    }
}
