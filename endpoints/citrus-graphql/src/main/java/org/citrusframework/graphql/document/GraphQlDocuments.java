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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import graphql.GraphQLError;
import graphql.ParseAndValidate;
import graphql.language.AstPrinter;
import graphql.language.Document;
import graphql.language.OperationDefinition;
import graphql.language.OperationDefinition.Operation;
import graphql.language.SourceLocation;
import graphql.parser.InvalidSyntaxException;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import graphql.validation.ValidationError;
import org.citrusframework.exceptions.ValidationException;

/**
 * Parses, inspects, validates and compares GraphQL documents. All rule violations are reported as
 * {@link ValidationException} with source locations where available.
 */
public final class GraphQlDocuments {

    private GraphQlDocuments() {
        // utility class
    }

    /**
     * Parses a GraphQL document.
     * @throws ValidationException when the document is empty or not syntactically valid
     */
    public static Document parse(String document) {
        if (document == null || document.isBlank()) {
            throw new ValidationException("Invalid GraphQL document: document is empty");
        }

        try {
            return Parser.parse(document);
        } catch (InvalidSyntaxException e) {
            SourceLocation location = e.getLocation();
            throw new ValidationException(location == null
                    ? "Invalid GraphQL document: %s".formatted(e.getMessage())
                    : "Invalid GraphQL document %s: %s".formatted(location(location), e.getMessage()), e);
        }
    }

    /**
     * Selects the operation a request executes: the named one, or the only one when no name is given.
     * @throws ValidationException when the operation cannot be determined
     */
    public static GraphQlOperation selectOperation(Document document, String operationName) {
        List<OperationDefinition> operations = document.getDefinitionsOfType(OperationDefinition.class);
        if (operations.isEmpty()) {
            throw new ValidationException("GraphQL document contains no operation");
        }

        if (operationName == null || operationName.isBlank()) {
            if (operations.size() > 1) {
                throw new ValidationException("GraphQL document contains several operations (%s) - an operation name is required"
                        .formatted(names(operations)));
            }

            return toOperation(operations.get(0));
        }

        return operations.stream()
                .filter(operation -> operationName.equals(operation.getName()))
                .findFirst()
                .map(GraphQlDocuments::toOperation)
                .orElseThrow(() -> new ValidationException("GraphQL document has no operation named '%s' (operations: %s)"
                        .formatted(operationName, names(operations))));
    }

    /**
     * Only queries may be sent over HTTP GET (GraphQL over HTTP).
     * @throws ValidationException for any other operation type
     */
    public static void checkGetAllowed(GraphQlOperation operation) {
        if (operation.type() != Operation.QUERY) {
            throw new ValidationException("GraphQL %s operations cannot be sent over GET - only queries can"
                    .formatted(operation.typeName()));
        }
    }

    /**
     * Validates a document against a schema.
     * @throws ValidationException listing every validation error with its location
     */
    public static void validate(GraphQLSchema schema, Document document) {
        List<ValidationError> errors = ParseAndValidate.validate(schema, document);
        if (!errors.isEmpty()) {
            throw new ValidationException("GraphQL document does not match the schema: %s".formatted(errors.stream()
                    .map(GraphQlDocuments::describe)
                    .collect(Collectors.joining("; "))));
        }
    }

    /**
     * Prints a document in a canonical compact form: whitespace, commas and comments are dropped,
     * everything else (including string literals) is kept.
     */
    public static String normalize(String document) {
        return AstPrinter.printAstCompact(parse(document));
    }

    /**
     * Compares two documents ignoring whitespace, comments and formatting.
     */
    public static boolean equivalent(String expected, String actual) {
        return Objects.equals(normalize(expected), normalize(actual));
    }

    /**
     * Describes a GraphQL error, prefixed with the location of its first source position, if any.
     */
    static String describe(GraphQLError error) {
        List<SourceLocation> locations = error.getLocations();
        if (locations == null || locations.isEmpty()) {
            return error.getMessage();
        }

        return "%s %s".formatted(location(locations.get(0)), error.getMessage());
    }

    private static String location(SourceLocation location) {
        return "(line %d, column %d)".formatted(location.getLine(), location.getColumn());
    }

    private static GraphQlOperation toOperation(OperationDefinition definition) {
        return new GraphQlOperation(definition.getName(), definition.getOperation());
    }

    private static String names(List<OperationDefinition> operations) {
        return operations.stream()
                .map(operation -> Objects.requireNonNullElse(operation.getName(), "<anonymous>"))
                .collect(Collectors.joining(", "));
    }
}
