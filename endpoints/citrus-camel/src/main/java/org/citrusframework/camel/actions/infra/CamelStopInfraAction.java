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

package org.citrusframework.camel.actions.infra;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import org.apache.camel.impl.DefaultCamelContext;
import org.citrusframework.actions.camel.CamelInfraStopActionBuilder;
import org.citrusframework.camel.actions.AbstractCamelAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action stops Camel infra instance.
 */
public class CamelStopInfraAction extends AbstractCamelAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CamelStopInfraAction.class);

    private final Object instance;
    private final String serviceName;
    private final String implementation;

    protected CamelStopInfraAction(Builder builder) {
        super("stop-infra", builder);

        this.instance = builder.instance;
        this.serviceName = builder.serviceName;
        this.implementation = builder.implementation;
    }

    @Override
    public void doExecute(TestContext context) {
        String resolvedServiceName = context.replaceDynamicContentInString(serviceName);
        String resolvedImplementation = Optional.ofNullable(implementation).map(context::replaceDynamicContentInString).orElse(null);
        String fullServiceName = StringUtils.hasText(resolvedImplementation) ? resolvedServiceName + "." + resolvedImplementation : resolvedServiceName;

        try {
            Object instanceToStop = instance;
            if (instanceToStop == null) {
                if (!context.getVariables().containsKey("%s%s".formatted(CamelInfraSettings.CAMEL_INFRA_PROPERTY_PREFIX, fullServiceName))) {
                    throw new CitrusRuntimeException("No such Camel infra service '" + fullServiceName + "' in current test context");
                }

                instanceToStop = context.getVariable("%s%s".formatted(CamelInfraSettings.CAMEL_INFRA_PROPERTY_PREFIX, fullServiceName), Object.class);
            }

            // Call shutdown method on infra service
            instanceToStop.getClass().getMethod("shutdown").invoke(instanceToStop);

            logger.info("Stopped Camel infra service '{}'", fullServiceName);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new CitrusRuntimeException("Failed to stop Camel infra service '%s'".formatted(fullServiceName), e);
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractCamelAction.Builder<CamelStopInfraAction, Builder>
            implements CamelInfraStopActionBuilder<CamelStopInfraAction, Builder> {

        private Object instance;
        private InfraService meta;
        private String serviceName;
        private String implementation;

        public Builder() {
            // Camel context is optional for this action - set a default context so users do not have to set it explicitly
            camelContext = new DefaultCamelContext();
        }

        @Override
        public Builder service(String serviceName, String implementation) {
            this.serviceName = serviceName;
            this.implementation = implementation;
            return this;
        }

        @Override
        public Builder service(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        @Override
        public Builder implementation(String implementation) {
            this.implementation = implementation;
            return this;
        }

        @Override
        public Builder instance(Object instance) {
            this.instance = instance;
            return this;
        }

        @Override
        public Builder meta(Object o) {
            if (o instanceof InfraService infraService) {
                this.meta = infraService;
            } else {
                throw new CitrusRuntimeException("Invalid infra service meta object, expected InfraService but got %s".formatted(o.getClass().getName()));
            }

            return this;
        }

        public Builder meta(InfraService meta) {
            this.meta = meta;
            return this;
        }

        @Override
        protected CamelStopInfraAction doBuild() {
            if (meta != null) {
                if (!StringUtils.hasText(serviceName)) {
                    serviceName = meta.service();
                }

                if (!StringUtils.hasText(implementation)) {
                    implementation = meta.implementation();
                }
            }

            return new CamelStopInfraAction(this);
        }
    }
}
