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

package org.citrusframework.openapi.validation;

import java.util.List;
import javax.annotation.Nullable;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.ApiOperation;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.Response;
import com.atlassian.oai.validator.report.MessageResolver;
import com.atlassian.oai.validator.report.ValidationReport.Message;
import com.atlassian.oai.validator.schema.SchemaValidator;
import com.atlassian.oai.validator.schema.SwaggerV20Library;
import com.atlassian.oai.validator.whitelist.ValidationErrorsWhitelist;
import com.atlassian.oai.validator.whitelist.rule.WhitelistRule;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.annotation.Nonnull;

import static org.citrusframework.openapi.OpenApiSettings.isRequestValidationEnabledGlobally;
import static org.citrusframework.openapi.OpenApiSettings.isResponseValidationEnabledGlobally;

/**
 * Represents the context for OpenAPI validation, providing configuration and validators for request and response validation.
 * This context maintains settings for both request and response validation, which can be controlled at an instance level.
 * By default, these settings are initialized based on a global configuration.
 */
public class OpenApiValidationContext {

    private static final List<IgnoreByKeyWhitelistRule> WHITELIST_RULES = List.of(
        // Filtered because in general OpenAPI is not required to specify all response codes.
        // So this should not be considered as validation error.
        IgnoreByKeyWhitelistRule.ignoreByKey("Allow unknown response status rule", "validation.response.status.unknown")
    );

    private final OpenAPI openApi;

    private OpenApiInteractionValidator openApiInteractionValidator;

    private SchemaValidator schemaValidator;

    private boolean responseValidationEnabled = isResponseValidationEnabledGlobally();

    private boolean requestValidationEnabled = isRequestValidationEnabledGlobally();

    public OpenApiValidationContext(OpenAPI openApi) {
        this.openApi = openApi;
    }

    public OpenAPI getSwaggerOpenApi() {
        return openApi;
    }

    private static final ValidationErrorsWhitelist validationErrorsWhitelist;

    static {
        ValidationErrorsWhitelist whiteList = ValidationErrorsWhitelist.create();
        for (IgnoreByKeyWhitelistRule rule : WHITELIST_RULES) {
            whiteList = whiteList.withRule(rule.name, rule);
        }
        validationErrorsWhitelist = whiteList;
    }

    public synchronized @Nonnull OpenApiInteractionValidator getOpenApiInteractionValidator() {
        if (openApiInteractionValidator == null) {
            openApiInteractionValidator = new OpenApiInteractionValidator.Builder()
                .withApi(openApi)
                .withWhitelist(validationErrorsWhitelist)
                .build();
        }
        return openApiInteractionValidator;
    }

    public synchronized @Nonnull SchemaValidator getSchemaValidator() {
        if (schemaValidator == null) {
            schemaValidator = new SchemaValidator(openApi, new MessageResolver(), SwaggerV20Library::schemaFactory);
        }
        return schemaValidator;
    }

    public boolean isResponseValidationEnabled() {
        return responseValidationEnabled;
    }

    public void setResponseValidationEnabled(boolean responseValidationEnabled) {
        this.responseValidationEnabled = responseValidationEnabled;
    }

    public boolean isRequestValidationEnabled() {
        return requestValidationEnabled;
    }

    public void setRequestValidationEnabled(boolean requestValidationEnabled) {
        this.requestValidationEnabled = requestValidationEnabled;
    }

    private static class IgnoreByKeyWhitelistRule implements WhitelistRule {

        private final String name;

        private final String key;

        private IgnoreByKeyWhitelistRule(@Nonnull String name, @Nonnull String key) {
            this.name = name;
            this.key = key;
        }

        @Override
        public boolean matches(Message message, @Nullable ApiOperation operation,
            @Nullable Request request, @Nullable Response response) {
            return key.equals(message.getKey());
        }

        public static IgnoreByKeyWhitelistRule ignoreByKey(String name, String key) {
            return new IgnoreByKeyWhitelistRule(name, key);
        }
    }

}
