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

import java.util.List;

import org.apache.camel.api.management.ManagedCamelContext;
import org.apache.camel.api.management.mbean.ManagedRouteMBean;
import org.citrusframework.api.actions.camel.CamelVerifyRouteStatsActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.context.json.JsonMessageValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CamelVerifyRouteStatsAction extends AbstractCamelRouteAction {

    private static final Logger logger = LoggerFactory.getLogger(CamelVerifyRouteStatsAction.class);

    private final String routeId;
    private final Long completed;
    private final Long failed;
    private final String expectedStatsJson;

    public CamelVerifyRouteStatsAction(Builder builder) {
        super("verify-route-stats", builder);

        this.routeId = builder.routeId;
        this.completed = builder.completed;
        this.failed = builder.failed;
        this.expectedStatsJson = builder.expectedStatsJson;
    }

    @Override
    public void doExecute(TestContext context) {
        String resolvedRouteId = context.replaceDynamicContentInString(routeId);

        ManagedCamelContext managedContext = camelContext.getCamelContextExtension()
                .getContextPlugin(ManagedCamelContext.class);

        if (managedContext == null) {
            throw new CitrusRuntimeException(
                    "Failed to get managed Camel context extension - make sure camel-management is on the classpath");
        }

        ManagedRouteMBean routeMBean = managedContext.getManagedRoute(resolvedRouteId);

        if (routeMBean == null) {
            throw new CitrusRuntimeException(
                    "Failed to get managed route statistics for routeId '%s'"
                            .formatted(resolvedRouteId));
        }

        if (completed != null) {
            long actualCompleted = routeMBean.getExchangesCompleted();
            if (actualCompleted != completed) {
                throw new ValidationException(
                        ("Route with routeId '%s' has %d completed exchanges " +
                                "and did not match the expected value %d")
                                .formatted(resolvedRouteId, actualCompleted, completed));
            }
            logger.info("Verified route '{}' has {} completed exchanges", resolvedRouteId, completed);
        }

        if (failed != null) {
            long actualFailed = routeMBean.getExchangesFailed();
            if (actualFailed != failed) {
                throw new ValidationException(
                        ("Route with routeId '%s' has %d failed exchanges " +
                                "and did not match the expected value %d")
                                .formatted(resolvedRouteId, actualFailed, failed));
            }
            logger.info("Verified route '{}' has {} failed exchanges", resolvedRouteId, failed);
        }

        if (StringUtils.hasText(expectedStatsJson)) {
            try {
                String actualStatsJson = routeMBean.dumpStatsAsJSon(false);
                String resolvedExpectedJson = context.replaceDynamicContentInString(expectedStatsJson);

                Message receivedMessage = new DefaultMessage(actualStatsJson)
                        .setType(MessageType.JSON);
                Message controlMessage = new DefaultMessage(resolvedExpectedJson)
                        .setType(MessageType.JSON);

                MessageValidator<? extends ValidationContext> validator = MessageValidator.lookup("json")
                        .orElseThrow(() -> new CitrusRuntimeException(
                                "No JSON message validator found - make sure citrus-validation-json is on the classpath"));

                validator.validateMessage(receivedMessage, controlMessage, context,
                        List.of(new JsonMessageValidationContext.Builder()
                                .schemaValidation(false)
                                .strict(false)
                                .build()));

                logger.info("Verified route '{}' statistics JSON matches expected values", resolvedRouteId);
            } catch (ValidationException e) {
                throw e;
            } catch (Exception e) {
                throw new CitrusRuntimeException(
                        "Failed to verify route statistics JSON for routeId '%s'"
                                .formatted(resolvedRouteId), e);
            }
        }
    }

    public String getRouteId() {
        return routeId;
    }

    public Long getCompleted() {
        return completed;
    }

    public Long getFailed() {
        return failed;
    }

    public String getExpectedStatsJson() {
        return expectedStatsJson;
    }

    public static final class Builder extends AbstractCamelRouteAction.Builder<CamelVerifyRouteStatsAction, Builder>
            implements CamelVerifyRouteStatsActionBuilder<CamelVerifyRouteStatsAction, Builder> {

        private String routeId;
        private Long completed;
        private Long failed;
        private String expectedStatsJson;

        @Override
        public Builder route(String routeId) {
            this.routeId = routeId;
            return this;
        }

        @Override
        public Builder completed(long completed) {
            this.completed = completed;
            return this;
        }

        @Override
        public Builder failed(long failed) {
            this.failed = failed;
            return this;
        }

        @Override
        public Builder stats(String expectedStatsJson) {
            this.expectedStatsJson = expectedStatsJson;
            return this;
        }

        @Override
        public CamelVerifyRouteStatsAction doBuild() {
            return new CamelVerifyRouteStatsAction(this);
        }
    }
}
