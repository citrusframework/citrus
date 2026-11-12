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

package org.citrusframework.testcontainers;

import org.citrusframework.api.yaml.TestVariable;

public enum TestcontainersTestVariables implements TestVariable {
    HOST("HOST", "Container host address."),
    CONTAINER_IP("CONTAINER_IP", "Container IP address."),
    CONTAINER_ID("CONTAINER_ID", "Docker container ID (first 12 characters)."),
    CONTAINER_NAME("CONTAINER_NAME", "Docker container name."),
    PORT("PORT", "Mapped container port.");

    private final String suffix;
    private final String description;

    TestcontainersTestVariables(String suffix, String description) {
        this.suffix = suffix;
        this.description = description;
    }

    @Override
    public String variableName() {
        return TestContainersSettings.TESTCONTAINERS_VARIABLE_PREFIX + "{containerType}_" + suffix;
    }

    public String suffix() {
        return suffix;
    }

    @Override
    public String description() {
        return description;
    }
}
