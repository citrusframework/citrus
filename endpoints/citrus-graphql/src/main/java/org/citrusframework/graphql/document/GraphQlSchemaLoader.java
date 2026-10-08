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
package org.citrusframework.graphql.document;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import graphql.GraphQLError;
import graphql.language.SourceLocation;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import graphql.schema.idl.errors.SchemaProblem;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.graphql.execution.GraphQlSource;

/**
 * Loads a GraphQL schema from one or more SDL resources for validating operations. The resources are
 * merged by Spring GraphQL's {@link GraphQlSource} schema builder (so types and type extensions may be
 * spread across files) and the schema is created without any runtime wiring: it can validate
 * documents but never execute them. Custom scalars get a placeholder implementation that accepts any
 * literal.
 * <p>
 * The schema is built lazily on first access and cached; concurrent first calls build it once.
 */
public class GraphQlSchemaLoader {

    private final List<String> resourcePaths;

    private volatile GraphQLSchema schema;

    public GraphQlSchemaLoader(List<String> resourcePaths) {
        if (resourcePaths == null || resourcePaths.isEmpty()) {
            throw new IllegalArgumentException("At least one GraphQL schema resource is required");
        }

        this.resourcePaths = List.copyOf(resourcePaths);
    }

    /**
     * Gets the schema, building it from the configured resources on first access.
     * @throws CitrusRuntimeException naming the offending resource when a resource is missing or invalid
     */
    public GraphQLSchema getSchema() {
        GraphQLSchema result = schema;
        if (result == null) {
            synchronized (this) {
                result = schema;
                if (result == null) {
                    result = load();
                    schema = result;
                }
            }
        }

        return result;
    }

    public List<String> getResourcePaths() {
        return resourcePaths;
    }

    private GraphQLSchema load() {
        org.springframework.core.io.Resource[] schemaResources = resourcePaths.stream()
                .map(GraphQlSchemaLoader::read)
                .toArray(org.springframework.core.io.Resource[]::new);

        try {
            return GraphQlSource.schemaResourceBuilder()
                    .schemaResources(schemaResources)
                    .schemaFactory((registry, wiring) -> UnExecutableSchemaGenerator.makeUnExecutableSchema(registry))
                    .build()
                    .schema();
        } catch (SchemaProblem e) {
            throw new CitrusRuntimeException("Invalid GraphQL schema built from %s: %s".formatted(resourcePaths, describe(e)), e);
        } catch (IllegalStateException e) {
            if (e.getCause() instanceof SchemaProblem problem) {
                throw new CitrusRuntimeException("Invalid GraphQL schema '%s': %s".formatted(failedResource(e), describe(problem)), e);
            }

            throw e;
        }
    }

    /**
     * Spring GraphQL reports a parse failure with the resource description in the exception message;
     * the description of every resource is its Citrus resource path.
     */
    private String failedResource(IllegalStateException e) {
        String message = String.valueOf(e.getMessage());
        return resourcePaths.stream()
                .filter(message::contains)
                .findFirst()
                .orElse(String.join(", ", resourcePaths));
    }

    private static org.springframework.core.io.Resource read(String resourcePath) {
        Resource resource = Resources.create(resourcePath);
        if (!resource.exists()) {
            throw new CitrusRuntimeException("GraphQL schema resource '%s' does not exist".formatted(resourcePath));
        }

        try (InputStream inputStream = resource.getInputStream()) {
            return new ByteArrayResource(inputStream.readAllBytes(), resourcePath);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read GraphQL schema resource '%s'".formatted(resourcePath), e);
        }
    }

    private static String describe(SchemaProblem problem) {
        return problem.getErrors().stream()
                .map(GraphQlSchemaLoader::describe)
                .collect(Collectors.joining("; "));
    }

    /**
     * Describes a GraphQL error with the location of its first source position, if any.
     */
    static String describe(GraphQLError error) {
        List<SourceLocation> locations = error.getLocations();
        if (locations == null || locations.isEmpty()) {
            return error.getMessage();
        }

        SourceLocation location = locations.get(0);
        return "(line %d, column %d) %s".formatted(location.getLine(), location.getColumn(), error.getMessage());
    }
}
