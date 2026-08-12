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

package org.citrusframework.camel.actions;

import org.apache.camel.ServiceStatus;
import org.citrusframework.api.actions.camel.CamelVerifyRouteActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CamelVerifyRouteAction extends AbstractCamelRouteAction {

    private static final Logger logger = LoggerFactory.getLogger(CamelVerifyRouteAction.class);

    private final String routeId;
    private final String status;

    public CamelVerifyRouteAction(Builder builder) {
        super("verify-route", builder);

        this.routeId = builder.routeId;
        this.status = builder.status;
    }

    @Override
    public void doExecute(TestContext context) {
        String resolvedRouteId = context.replaceDynamicContentInString(routeId);

        if (camelContext.getRoute(resolvedRouteId) == null) {
            throw new CitrusRuntimeException(
                    "Camel route '%s' does not exist in context '%s'"
                            .formatted(resolvedRouteId, camelContext.getName()));
        }

        logger.info("Verified Camel route '{}' exists in context '{}'", resolvedRouteId, camelContext.getName());

        if (StringUtils.hasText(status)) {
            String expectedStatus = context.replaceDynamicContentInString(status);
            ServiceStatus routeStatus = camelContext.getRouteController().getRouteStatus(resolvedRouteId);

            if (routeStatus == null) {
                throw new CitrusRuntimeException(
                        "Unable to retrieve status for Camel route '%s' in context '%s'"
                                .formatted(resolvedRouteId, camelContext.getName()));
            }

            ValidationUtils.validateValues(routeStatus.name(), expectedStatus, "camelRouteStatus", context);
            logger.info("Verified Camel route '{}' has status '{}'", resolvedRouteId, expectedStatus);
        }
    }

    public String getRouteId() {
        return routeId;
    }

    public String getStatus() {
        return status;
    }

    public static final class Builder extends AbstractCamelRouteAction.Builder<CamelVerifyRouteAction, Builder>
            implements CamelVerifyRouteActionBuilder<CamelVerifyRouteAction, Builder> {

        private String routeId;
        private String status;

        @Override
        public Builder route(String routeId) {
            this.routeId = routeId;
            return this;
        }

        public Builder status(ServiceStatus status) {
            this.status = status.name();
            return this;
        }

        @Override
        public Builder status(Enum<?> status) {
            if (status instanceof ServiceStatus serviceStatus) {
                this.status = serviceStatus.name();
            }
            return this;
        }

        @Override
        public Builder status(String status) {
            this.status = status;
            return this;
        }

        @Override
        public CamelVerifyRouteAction doBuild() {
            return new CamelVerifyRouteAction(this);
        }
    }
}
