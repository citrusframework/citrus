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

package org.citrusframework.camel.yaml;

import org.citrusframework.camel.actions.AbstractCamelAction;
import org.citrusframework.camel.actions.infra.CamelRunInfraAction;
import org.citrusframework.camel.actions.infra.CamelStopInfraAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.yaml.SchemaProperty;

import static org.citrusframework.yaml.SchemaProperty.Kind.ACTION;

public class Infra implements CamelActionBuilderWrapper<AbstractCamelAction.Builder<?, ?>> {

    private static final String CAMEL_INFRA_GROUP = "camel-infra";

    private AbstractCamelAction.Builder<?, ?> builder;

    @SchemaProperty(kind = ACTION, group = CAMEL_INFRA_GROUP, description = "Runs a Camel infra service.")
    public void setRun(Run run) {
        this.builder = run.getBuilder();
    }

    @SchemaProperty(kind = ACTION, group = CAMEL_INFRA_GROUP, description = "Stops a Camel infra service.")
    public void setStop(Stop stop) {
        this.builder = stop.getBuilder();
    }

    @Override
    public AbstractCamelAction.Builder<?, ?> getBuilder() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing Camel infra action specification");
        }

        return builder;
    }

    public static class Run implements CamelActionBuilderWrapper<CamelRunInfraAction.Builder> {

        private final CamelRunInfraAction.Builder builder = new CamelRunInfraAction.Builder();

        @SchemaProperty(advanced = true, description = "The Camel catalog that holds the infra service definitions.")
        public void setCatalog(String camelCatalog) {
            builder.catalog(camelCatalog);
        }

        @SchemaProperty(description = "The Camel infra service name.")
        public void setService(String serviceName) {
            builder.service(serviceName);
        }

        @SchemaProperty(description = "Optional service implementation detail.")
        public void setImplementation(String implementation) {
            builder.implementation(implementation);
        }

        @SchemaProperty(description = "When enabled the infra service is automatically stopped after the test.")
        public void setAutoRemove(boolean autoRemove) {
            builder.autoRemove(autoRemove);
        }

        @SchemaProperty(advanced = true, description = "When enabled the service output is saved into a log file.")
        public void setDumpServiceOutput(boolean enabled) {
            builder.dumpServiceOutput(enabled);
        }

        @Override
        public CamelRunInfraAction.Builder getBuilder() {
            return builder;
        }
    }

    public static class Stop implements CamelActionBuilderWrapper<CamelStopInfraAction.Builder> {

        private final CamelStopInfraAction.Builder builder = new CamelStopInfraAction.Builder();

        @SchemaProperty(description = "The Camel infra service name.")
        public void setService(String serviceName) {
            builder.service(serviceName);
        }

        @SchemaProperty(description = "Optional service implementation detail.")
        public void setImplementation(String implementation) {
            builder.implementation(implementation);
        }

        @Override
        public CamelStopInfraAction.Builder getBuilder() {
            return builder;
        }
    }
}
