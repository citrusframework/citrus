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

import graphql.language.Document;
import graphql.language.OperationDefinition.Operation;
import graphql.schema.GraphQLSchema;
import org.citrusframework.exceptions.ValidationException;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GraphQlDocumentsTest {

    private static final GraphQLSchema SCHEMA = new GraphQlSchemaLoader(List.of(
            "classpath:org/citrusframework/graphql/schema/query.graphqls",
            "classpath:org/citrusframework/graphql/schema/book.graphqls",
            "classpath:org/citrusframework/graphql/schema/scalars.graphqls")).getSchema();

    @Test
    public void shouldParseDocument() {
        Document document = GraphQlDocuments.parse("query Book($id: ID!) { book(id: $id) { title } }");

        assertThat(document.getDefinitions()).hasSize(1);
    }

    @Test
    public void shouldReportSyntaxErrorWithLocation() {
        assertThatThrownBy(() -> GraphQlDocuments.parse("query {\n  book(id: 1) { title }\n"))
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("Invalid GraphQL document (line 3, column 1): ");
    }

    @Test
    public void shouldRejectEmptyDocument() {
        assertThatThrownBy(() -> GraphQlDocuments.parse("  "))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Invalid GraphQL document: document is empty");
    }

    @Test
    public void shouldSelectSingleAnonymousOperation() {
        GraphQlOperation operation = GraphQlDocuments.selectOperation(GraphQlDocuments.parse("{ books { title } }"), null);

        assertThat(operation.name()).isNull();
        assertThat(operation.type()).isEqualTo(Operation.QUERY);
    }

    @Test
    public void shouldSelectSingleNamedOperationWithoutGivenName() {
        GraphQlOperation operation = GraphQlDocuments.selectOperation(
                GraphQlDocuments.parse("mutation AddBook { addBook { id } }"), null);

        assertThat(operation.name()).isEqualTo("AddBook");
        assertThat(operation.type()).isEqualTo(Operation.MUTATION);
    }

    @Test
    public void shouldSelectNamedOperationAmongSeveral() {
        GraphQlOperation operation = GraphQlDocuments.selectOperation(
                GraphQlDocuments.parse("query A { a } mutation B { b }"), "B");

        assertThat(operation.name()).isEqualTo("B");
        assertThat(operation.type()).isEqualTo(Operation.MUTATION);
    }

    @Test
    public void shouldIgnoreFragmentsWhenSelectingOperation() {
        GraphQlOperation operation = GraphQlDocuments.selectOperation(GraphQlDocuments.parse("""
                query Book { book(id: 1) { ...Fields } }
                fragment Fields on Book { title }"""), null);

        assertThat(operation.name()).isEqualTo("Book");
    }

    @Test
    public void shouldRequireOperationNameForSeveralOperations() {
        Document document = GraphQlDocuments.parse("query A { a } query B { b }");

        assertThatThrownBy(() -> GraphQlDocuments.selectOperation(document, null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("GraphQL document contains several operations (A, B) - an operation name is required");
    }

    @Test
    public void shouldRejectUnknownOperationName() {
        Document document = GraphQlDocuments.parse("query A { a } query B { b }");

        assertThatThrownBy(() -> GraphQlDocuments.selectOperation(document, "C"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("GraphQL document has no operation named 'C' (operations: A, B)");
    }

    @Test
    public void shouldRejectDocumentWithoutOperation() {
        Document document = GraphQlDocuments.parse("fragment Fields on Book { title }");

        assertThatThrownBy(() -> GraphQlDocuments.selectOperation(document, null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("GraphQL document contains no operation");
    }

    @Test
    public void shouldAllowQueryOverGet() {
        GraphQlOperation operation = GraphQlDocuments.selectOperation(GraphQlDocuments.parse("{ books { title } }"), null);

        GraphQlDocuments.checkGetAllowed(operation);
    }

    @Test
    public void shouldRejectMutationOverGet() {
        GraphQlOperation operation = GraphQlDocuments.selectOperation(
                GraphQlDocuments.parse("mutation { deleteBook(id: 1) }"), null);

        assertThatThrownBy(() -> GraphQlDocuments.checkGetAllowed(operation))
                .isInstanceOf(ValidationException.class)
                .hasMessage("GraphQL mutation operations cannot be sent over GET - only queries can");
    }

    @Test
    public void shouldValidateAgainstSchema() {
        GraphQlDocuments.validate(SCHEMA, GraphQlDocuments.parse("query Book($id: ID!) { book(id: $id) { title published } }"));
    }

    @Test
    public void shouldReportSchemaViolationsWithLocation() {
        Document document = GraphQlDocuments.parse("{ book(id: 1) { isbn } }");

        assertThatThrownBy(() -> GraphQlDocuments.validate(SCHEMA, document))
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("GraphQL document does not match the schema: (line 1, column 17) ")
                .hasMessageContaining("isbn");
    }

    @Test
    public void shouldTreatFormattingAndCommentsAsEquivalent() {
        assertThat(GraphQlDocuments.equivalent(
                "query Book($id:ID!){book(id:$id){title}}",
                """
                # fetch a book
                query Book($id: ID!) {
                  book(id: $id) {
                    title # the title
                  }
                }""")).isTrue();
    }

    @Test
    public void shouldTreatDifferentFieldsAsNotEquivalent() {
        assertThat(GraphQlDocuments.equivalent("{ book(id: 1) { title } }", "{ book(id: 1) { id } }")).isFalse();
    }

    @Test
    public void shouldKeepStringLiteralsWhenComparing() {
        assertThat(GraphQlDocuments.equivalent("{ books(filter: \"a  b\") { title } }", "{ books(filter: \"a b\") { title } }")).isFalse();
    }

    @Test
    public void shouldKeepFragmentsAndDirectivesWhenNormalizing() {
        String normalized = GraphQlDocuments.normalize("""
                query Book($withId: Boolean!) {
                  book(id: 1) { ...Fields id @include(if: $withId) }
                }
                fragment Fields on Book { title }""");

        assertThat(normalized).contains("...Fields").contains("@include(if:$withId)").contains("fragment Fields on Book");
    }
}
