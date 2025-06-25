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

import java.util.LinkedHashMap;
import java.util.Map;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.CreateEndpointAction;

public class CreateEndpoint implements TestActionBuilder<CreateEndpointAction> {

    private final CreateEndpointAction.Builder builder = new CreateEndpointAction.Builder();

    protected String type;
    protected String uri;
    protected String name;
    protected final Map<String, String> properties = new LinkedHashMap<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
    }

    @Override
    public CreateEndpointAction build() {
        builder.type(type);

        if (uri != null) {
            builder.uri(uri);
        }

        if (name != null) {
            builder.endpointName(name);
        }

        builder.properties(properties);

        return builder.build();
    }

    public void setDescription(String value) {
        builder.description(value);
    }

}
