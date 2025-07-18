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

package org.citrusframework.openapi.util;

import java.util.Collection;
import java.util.Optional;

import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasSchema;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.openapi.OpenApiConstants;
import org.citrusframework.openapi.OpenApiRepository;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.model.OasModelHelper;
import org.citrusframework.spi.ReferenceResolver;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.citrusframework.message.MessageType.JSON;
import static org.citrusframework.openapi.model.OasModelHelper.resolveSchema;
import static org.citrusframework.util.StringUtils.hasText;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public final class OpenApiUtils {

    private OpenApiUtils() {
        // Static access only
    }

    public static String getMethodPath(@Nonnull String method, @Nonnull String path) {
        if (hasText(path) && path.startsWith("/")) {
            path = path.substring(1);
        }
        return format("%s_/%s", method.toUpperCase(), path);
    }

    /**
     * @return a unique scenario id for the {@link OasOperation}
     */
    public static String createFullPathOperationIdentifier(OasOperation oasOperation, String path) {
        return createFullPathOperationIdentifier(oasOperation.getMethod(), path);
    }

    /**
     * @return a unique scenario id for the {@link OasOperation}
     */
    public static String createFullPathOperationIdentifier(String method, String path) {
        return format("%s_%s", method.toUpperCase(), path);
    }

    public static boolean isAnyNumberScheme(@Nullable OasSchema schema) {
        return schema != null
                && (OpenApiConstants.TYPE_INTEGER.equalsIgnoreCase(schema.type)
                || OpenApiConstants.TYPE_NUMBER.equalsIgnoreCase(schema.type));
    }

    /**
     * Checks if given field name is in list of required fields for this schema.
     */
    public static boolean isRequired(OasSchema schema, String field) {
        if (schema.required == null) {
            return true;
        }

        return schema.required.contains(field);
    }

    /**
     * Retrieves all known OpenAPI aliases from {@link org.citrusframework.openapi.OpenApiSpecification}s
     * registered in {@link OpenApiRepository}s.
     *
     * @param resolver the {@code ReferenceResolver} to use for resolving {@code OpenApiRepository} instances.
     * @return a comma-separated string of all known OpenAPI aliases.
     */
    public static String getKnownOpenApiAliases(ReferenceResolver resolver) {
        return resolver.resolveAll(OpenApiRepository.class).values()
                .stream()
                .flatMap(openApiRepository -> openApiRepository.getOpenApiSpecifications().stream())
                .flatMap(spec -> spec.getAliases().stream())
                .collect(joining(", "));
    }

    public static void fillMessageTypeFromResponse(OpenApiSpecification openApiSpecification,
        HttpMessage httpMessage,
        @Nullable OasOperation operation,
        @Nullable OasResponse response) {
        if (operation == null || response == null) {
            return;
        }

        Optional<OasSchema> responseSchema = OasModelHelper.getSchema(response);
        responseSchema.ifPresent(oasSchema -> {
                OasSchema resolvedSchema = resolveSchema(openApiSpecification.getOpenApiDoc(null), oasSchema);
                if (OasModelHelper.isObjectType(resolvedSchema) || OasModelHelper.isObjectArrayType(resolvedSchema)) {
                    Collection<String> responseTypes = OasModelHelper.getResponseTypes(operation,response);

                    if (responseTypes.contains(APPLICATION_JSON_VALUE)) {
                        httpMessage.setType(JSON);
                    }
                }
            }
        );
    }

}
