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

package org.citrusframework.endpoint.context;

/**
 * Combines all test context related endpoint builders.
 */
public class ContextEndpoints {

    /**
     * Private constructor because static instantiation method should be used.
     */
    private ContextEndpoints() {
    }

    /**
     * Static entry method for in memory endpoint builders.
     */
    public static ContextEndpoints context() {
        return new ContextEndpoints();
    }

    public MessageStoreEndpointBuilder messageStore() {
        return new MessageStoreEndpointBuilder();
    }
}
