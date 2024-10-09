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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;

/**
 * @since 2.7
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "apiVersion",
        "kind",
        "success"
})
public class DeleteResult {

    @NotNull
    @JsonProperty("apiVersion")
    private String apVersion;

    @NotNull
    @JsonProperty("kind")
    private String kind;

    @NotNull
    @JsonProperty("success")
    private Boolean success;

    /**
     * Gets the success.
     *
     * @return
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * Sets the success.
     *
     * @param success
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /**
     * Gets the api version.
     * @return
     */
    public String getApVersion() {
        return apVersion;
    }

    /**
     * Sets the api version.
     * @param apVersion
     */
    public void setApVersion(String apVersion) {
        this.apVersion = apVersion;
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
