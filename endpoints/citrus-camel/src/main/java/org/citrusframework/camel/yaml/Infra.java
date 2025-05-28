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

public class Infra implements CamelActionBuilderWrapper<AbstractCamelAction.Builder<?, ?>> {

    private AbstractCamelAction.Builder<?, ?> builder;

    public void setRun(Run run) {
        this.builder = run.getBuilder();
    }

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

        public void setCatalog(String camelCatalog) {
            builder.catalog(camelCatalog);
        }

        public void setService(String serviceName) {
            builder.service(serviceName);
        }

        public void setImplementation(String implementation) {
            builder.implementation(implementation);
        }

        public void setAutoRemove(boolean autoRemove) {
            builder.autoRemove(autoRemove);
        }

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

        public void setService(String serviceName) {
            builder.service(serviceName);
        }

        public void setImplementation(String implementation) {
            builder.implementation(implementation);
        }

        @Override
        public CamelStopInfraAction.Builder getBuilder() {
            return builder;
        }
    }
}
