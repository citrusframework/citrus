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
import org.citrusframework.openapi.OpenApiResourceLoader;
import org.citrusframework.spi.Resource;

import java.net.URL;

import static java.util.Collections.emptyList;

/**
 * Utility class for creation of an {@link OpenApiValidationContext}.
 */
public final class OpenApiValidationContextLoader {

    private OpenApiValidationContextLoader() {
        // Static access only
    }

    /**
     * Creates an OpenApiValidationContext from a secured OpenAPI web resource.
     *
     * @param url the URL of the secured OpenAPI web resource
     * @return the OpenApiValidationContext
     */
    public static OpenApiValidationContext fromSecuredWebResource(@Nonnull URL url) {
        return createValidationContext(new OpenApiLoader().loadApi(SpecSource.inline(OpenApiResourceLoader.rawFromSecuredWebResource(url)), emptyList(), defaultParseOptions()));
    }

    /**
     * Creates an OpenApiValidationContext from an OpenAPI web resource.
     *
     * @param url the URL of the OpenAPI web resource
     * @return the OpenApiValidationContext
     */
    public static OpenApiValidationContext fromWebResource(@Nonnull URL url) {
        return createValidationContext(new OpenApiLoader().loadApi(SpecSource.inline(OpenApiResourceLoader.rawFromWebResource(url)), emptyList(), defaultParseOptions()));
    }

    /**
     * Creates an OpenApiValidationContext from an OpenAPI file.
     *
     * @param resource the file resource containing the OpenAPI specification
     * @return the OpenApiValidationContext
     */
    public static OpenApiValidationContext fromFile(@Nonnull Resource resource) {
        return createValidationContext(new OpenApiLoader().loadApi(SpecSource.inline(OpenApiResourceLoader.rawFromFile(resource)), emptyList(), defaultParseOptions()));
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
