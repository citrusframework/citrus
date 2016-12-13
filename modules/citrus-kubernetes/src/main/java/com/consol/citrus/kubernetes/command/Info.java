/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.kubernetes.command;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.kubernetes.client.KubernetesClient;
import com.fasterxml.jackson.annotation.*;
import io.fabric8.kubernetes.client.Version;

import javax.validation.constraints.NotNull;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class Info extends AbstractKubernetesCommand<Info.InfoModel, Info> {

    /**
     * Default constructor initializing the command name.
     */
    public Info() {
        super("info");
    }

    @Override
    public void execute(KubernetesClient kubernetesClient, TestContext context) {
        InfoModel model = new InfoModel();
        model.setClientVersion(Version.clientVersion());
        model.setApiVersion(kubernetesClient.getEndpointConfiguration().getKubernetesClient().getApiVersion());
        model.setMasterUrl(kubernetesClient.getEndpointConfiguration().getKubernetesClient().getMasterUrl().toString());
        model.setNamespace(kubernetesClient.getEndpointConfiguration().getKubernetesClient().getNamespace());

        setCommandResult(model);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "clientVersion",
            "apiVersion",
            "masterUrl",
            "namespace"
    })
    public static class InfoModel {
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

        /**
         * Gets the value of the apiVersion property.
         *
         * @return the apiVersion
         */
        public String getApiVersion() {
            return apiVersion;
        }

        /**
         * Sets the apiVersion property.
         *
         * @param apiVersion
         */
        public void setApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
        }

        /**
         * Gets the value of the clientVersion property.
         *
         * @return the clientVersion
         */
        public String getClientVersion() {
            return clientVersion;
        }

        /**
         * Sets the clientVersion property.
         *
         * @param clientVersion
         */
        public void setClientVersion(String clientVersion) {
            this.clientVersion = clientVersion;
        }

        /**
         * Gets the value of the masterUrl property.
         *
         * @return the masterUrl
         */
        public String getMasterUrl() {
            return masterUrl;
        }

        /**
         * Sets the masterUrl property.
         *
         * @param masterUrl
         */
        public void setMasterUrl(String masterUrl) {
            this.masterUrl = masterUrl;
        }

        /**
         * Gets the value of the namespace property.
         *
         * @return the namespace
         */
        public String getNamespace() {
            return namespace;
        }

        /**
         * Sets the namespace property.
         *
         * @param namespace
         */
        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }
    }
}
