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

package org.citrusframework.kubernetes.command;

import com.fasterxml.jackson.annotation.*;
import io.fabric8.kubernetes.api.model.*;

import jakarta.validation.constraints.NotNull;
import org.citrusframework.actions.kubernetes.command.KubernetesInfoCommandResult;

/**
 * @since 2.7
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "clientVersion",
        "apiVersion",
        "kind",
        "masterUrl",
        "namespace"
})
public class InfoResult implements HasMetadata, KubernetesInfoCommandResult {

    @NotNull
    @JsonProperty("kind")
    private String kind = "Info";

    @NotNull
    @JsonProperty("apiVersion")
    private String apiVersion = "v1";

    @NotNull
    @JsonProperty("clientVersion")
    private String clientVersion;

    @NotNull
    @JsonProperty("masterUrl")
    private String masterUrl;

    @NotNull
    @JsonProperty("namespace")
    private String namespace;

    @Override
    public String getKind() {
        return kind;
    }

    @Override
    public String getApiVersion() {
        return apiVersion;
    }

    @Override
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public String getClientVersion() {
        return clientVersion;
    }

    @Override
    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    @Override
    public String getMasterUrl() {
        return masterUrl;
    }

    @Override
    public void setMasterUrl(String masterUrl) {
        this.masterUrl = masterUrl;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public ObjectMeta getMetadata() {
        return null;
    }

    @Override
    public void setMetadata(ObjectMeta objectMeta) {
    }
}
