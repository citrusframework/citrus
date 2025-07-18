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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.emptyList;
import static org.citrusframework.openapi.validation.OpenApiValidationPolicy.REPORT;
import static org.citrusframework.openapi.validation.OpenApiValidationPolicy.STRICT;

/**
 * Utility class for creation of an {@link OpenApiValidationContext}.
 */
public final class OpenApiValidationContextLoader {

    private static final Logger logger = LoggerFactory.getLogger(
        OpenApiValidationContextLoader.class);

    /**
     * Cache for APIS which validation errors have already been logged. Used to avoid multiple
     * validation error logging for the same api.
     */
    private static final Set<String> apisWithLoggedValidationErrors = new HashSet<>();

    private OpenApiValidationContextLoader() {
        // Static access only
    }

    /**
     * Creates an OpenApiValidationContext from an open api string.
     *
     * @param openApiSpecAsString the string representation of an OpenAPI
     * @param openApiValidationPolicy the policy to be used for parsing
     * @return the OpenApiValidationContext
     */
    public static OpenApiValidationContext fromSpec(String openApiSpecAsString,
        OpenApiValidationPolicy openApiValidationPolicy) {
        OpenAPIParser openAPIParser = new OpenAPIParser();
        return createValidationContext(
            handleSwaggerParserResult(openApiSpecAsString, openAPIParser.readContents(openApiSpecAsString, emptyList(),
                defaultParseOptions()), openApiValidationPolicy));
    }

    private static OpenAPI handleSwaggerParserResult(String identifier,
        SwaggerParseResult swaggerParseResult,
        OpenApiValidationPolicy openApiValidationPolicy) {
        logger.trace("Handling swagger parser result: {}", swaggerParseResult);

        if (swaggerParseResult == null) {
            throw new CitrusRuntimeException(
                "Unable to parse OpenApi from specSource: " + identifier);
        }

        if (hasParseErrors(swaggerParseResult)) {
            handleValidationException(identifier, openApiValidationPolicy,
                swaggerParseResult.getMessages());
        }

        return swaggerParseResult.getOpenAPI();
    }

    private static boolean hasParseErrors(@Nullable final SwaggerParseResult parseResult) {
        if (parseResult == null || parseResult.getOpenAPI() == null) {
            return true;
        }
        return parseResult.getMessages() != null && !parseResult.getMessages().isEmpty();
    }

    private static void handleValidationException(String identifier,
        OpenApiValidationPolicy openApiValidationPolicy, List<String> errorMessages) {
        if (REPORT.equals(openApiValidationPolicy)
            && !apisWithLoggedValidationErrors.contains(identifier)) {
            apisWithLoggedValidationErrors.add(identifier);
            logger.warn("OpenApi '{}' has validation errors {}", identifier, errorMessages);
        } else if (STRICT.equals(openApiValidationPolicy)) {
            throw new ValidationException(
                format(
                    """
                        The API '%s' has failed STRICT validation:
                        %s
                        """,
                    identifier,
                    join(",", errorMessages)
                )
            );
        }
    }

    private static OpenApiValidationContext createValidationContext(OpenAPI openApi) {
        return new OpenApiValidationContext(openApi);
    }

    private static ParseOptions defaultParseOptions() {
        final ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setResolveFully(true);
        parseOptions.setResolveCombinators(false);
        return parseOptions;
    }
}
