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

package org.citrusframework.kubernetes;

/**
 * Cluster types with different operator namespaces.
 */
public enum ClusterType {

    LOCAL(""),
    KUBERNETES("citrus-system"),
    OPENSHIFT("openshift-operators");

    private final String operatorNamespace;

    ClusterType(String operatorNamespace) {
        this.operatorNamespace = operatorNamespace;
    }

    public String operatorNamespace() {
        return operatorNamespace;
    }
}
