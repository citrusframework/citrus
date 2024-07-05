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

import com.atlassian.oai.validator.OpenApiInteractionValidator.SpecSource;
import com.atlassian.oai.validator.util.OpenApiLoader;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import jakarta.annotation.Nonnull;
import java.net.URL;
import java.util.Collections;
import org.citrusframework.openapi.OpenApiResourceLoader;
import org.citrusframework.spi.Resource;

/**
 * Utility class for loading Swagger OpenAPI specifications from various resources.
 */
public abstract class SwaggerOpenApiValidationContextLoader {

    private SwaggerOpenApiValidationContextLoader() {
        // Static access only
    }
    /**
     * Loads an OpenAPI specification from a secured web resource.
     *
     * @param url the URL of the secured web resource
     * @return the loaded OpenAPI specification
     */
    public static SwaggerOpenApiValidationContext fromSecuredWebResource(@Nonnull URL url) {
        return createValidationContext(new OpenApiLoader().loadApi(SpecSource.inline(OpenApiResourceLoader.rawFromSecuredWebResource(url)), Collections.emptyList(), defaultParseOptions()));
    }

    /**
     * Loads an OpenAPI specification from a web resource.
     *
     * @param url the URL of the web resource
     * @return the loaded OpenAPI specification
     */
    public static SwaggerOpenApiValidationContext fromWebResource(@Nonnull URL url) {
        return createValidationContext(new OpenApiLoader().loadApi(SpecSource.inline(OpenApiResourceLoader.rawFromWebResource(url)), Collections.emptyList(), defaultParseOptions()));
    }

    /**
     * Loads an OpenAPI specification from a file.
     *
     * @param resource the file resource containing the OpenAPI specification
     * @return the loaded OpenAPI specification
     */
    public static SwaggerOpenApiValidationContext fromFile(@Nonnull Resource resource) {
        return createValidationContext(new OpenApiLoader().loadApi(SpecSource.inline(OpenApiResourceLoader.rawFromFile(resource)), Collections.emptyList(), defaultParseOptions()));
    }

    private static SwaggerOpenApiValidationContext createValidationContext(OpenAPI openApi) {
        return new SwaggerOpenApiValidationContext(openApi);
    }

    private static ParseOptions defaultParseOptions() {
        final ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setResolveFully(true);
        parseOptions.setResolveCombinators(false);
        return parseOptions;
    }
}
