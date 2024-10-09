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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import jakarta.validation.constraints.NotNull;

/**
 * @since 2.7
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "apiVersion",
        "kind",
        "items"
})
public class ListResult<T> {

    @NotNull
    @JsonProperty("apiVersion")
    private String apiVersion = "v1";

    @NotNull
    @JsonProperty("kind")
    private String kind;

    @NotNull
    @JsonProperty
    private List<T> items;

    /**
     * Default constructor.
     */
    public ListResult() {
        super();
    }

    /**
     * Constructor using result model.
     * @param items
     */
    public ListResult(List<T> items) {
        if (items != null) {
            if (items instanceof HasMetadata kubernetesResource) {
                this.apiVersion = kubernetesResource.getApiVersion();
                this.kind = kubernetesResource.getKind();
            } else if (!items.isEmpty() && items.get(0) instanceof HasMetadata kubernetesResource) {
                this.apiVersion = kubernetesResource.getApiVersion();
                this.kind = kubernetesResource.getKind() + "List";
            }
        }

        this.items = items;
    }

    /**
     * Gets the items.
     * @return
     */
    public List<T> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }

        return items;
    }

    /**
     * Sets the items.
     * @param items
     */
    public void setItems(List<T> items) {
        this.items = items;
    }

    /**
     * Gets the api version.
     * @return
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Sets the api version.
     * @param apiVersion
     */
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * Gets the resource kind.
     * @return
     */
    public String getKind() {
        return kind;
    }

    /**
     * Sets the resource kind.
     * @param kind
     */
    public void setKind(String kind) {
        this.kind = kind;
    }
}
