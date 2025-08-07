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

package org.citrusframework.actions.kubernetes.command;

public interface KubernetesInfoCommandResult {

    /**
     * Gets the api version.
     */
    String getApiVersion();

    /**
     * Sets the api version.
     */
    void setApiVersion(String apiVersion);

    /**
     * Gets the resource kind.
     */
    String getKind();

    /**
     * Gets the value of the clientVersion property.
     */
    String getClientVersion();

    /**
     * Sets the clientVersion property.
     */
    void setClientVersion(String clientVersion);

    /**
     * Gets the value of the masterUrl property.
     */
    String getMasterUrl();

    /**
     * Sets the masterUrl property.
     */
    void setMasterUrl(String masterUrl);

    /**
     * Gets the value of the namespace property.
     */
    String getNamespace();

    /**
     * Sets the namespace property.
     */
    void setNamespace(String namespace);
}
