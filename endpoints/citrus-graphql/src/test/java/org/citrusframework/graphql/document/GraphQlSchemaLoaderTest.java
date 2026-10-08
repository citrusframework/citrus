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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import graphql.ParseAndValidate;
import graphql.parser.Parser;
import graphql.schema.GraphQLSchema;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GraphQlSchemaLoaderTest {

    private static final String SCHEMA_DIR = "classpath:org/citrusframework/graphql/schema/";

    @Test
    public void shouldLoadSingleSchemaFile() {
        GraphQLSchema schema = loader("library.graphqls").getSchema();

        assertThat(schema.getQueryType().getFieldDefinition("book")).isNotNull();
        assertThat(schema.getObjectType("Book").getFieldDefinition("title")).isNotNull();
    }

    @Test
    public void shouldMergeSchemaFiles() {
        GraphQLSchema schema = loader("query.graphqls", "book.graphqls", "scalars.graphqls").getSchema();

        assertThat(schema.getQueryType().getFieldDefinition("books")).isNotNull();
        assertThat(schema.getObjectType("Book").getFieldDefinition("published")).isNotNull();
    }

    @Test
    public void shouldApplyTypeExtensionsAcrossFiles() {
        GraphQLSchema schema = loader("query.graphqls", "book.graphqls", "scalars.graphqls").getSchema();

        assertThat(schema.getQueryType().getFieldDefinition("booksSince")).isNotNull();
    }

    @Test
    public void shouldAcceptCustomScalarLiteralsInQueries() {
        GraphQLSchema schema = loader("query.graphqls", "book.graphqls", "scalars.graphqls").getSchema();

        assertThat(ParseAndValidate.validate(schema, Parser.parse("{ booksSince(date: \"2026-01-01\") { title published } }")))
                .isEmpty();
    }

    @Test
    public void shouldReportUnknownFieldsAgainstLoadedSchema() {
        GraphQLSchema schema = loader("library.graphqls").getSchema();

        assertThat(ParseAndValidate.validate(schema, Parser.parse("{ book(id: 1) { isbn } }")))
                .extracting(error -> error.getMessage())
                .anySatisfy(message -> assertThat(message).contains("isbn"));
    }

    @Test
    public void shouldNameMissingResource() {
        GraphQlSchemaLoader loader = loader("missing.graphqls");

        assertThatThrownBy(loader::getSchema)
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("missing.graphqls");
    }

    @Test
    public void shouldNameResourceAndLineOfSyntaxError() {
        GraphQlSchemaLoader loader = loader("library.graphqls", "broken.graphqls");

        assertThatThrownBy(loader::getSchema)
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("broken.graphqls")
                .hasMessageContaining("line 4");
    }

    @Test
    public void shouldNameResourcesOfInconsistentSchema() {
        GraphQlSchemaLoader loader = loader("dangling.graphqls");

        assertThatThrownBy(loader::getSchema)
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("dangling.graphqls")
                .hasMessageContaining("Author");
    }

    @Test
    public void shouldRequireAtLeastOneResource() {
        assertThatThrownBy(() -> new GraphQlSchemaLoader(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldCacheSchema() {
        GraphQlSchemaLoader loader = loader("library.graphqls");

        assertThat(loader.getSchema()).isSameAs(loader.getSchema());
    }

    @Test
    public void shouldBuildSchemaOnceUnderConcurrentAccess() throws InterruptedException {
        GraphQlSchemaLoader loader = loader("query.graphqls", "book.graphqls", "scalars.graphqls");
        Set<GraphQLSchema> schemas = ConcurrentHashMap.newKeySet();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            for (int i = 0; i < 8; i++) {
                executor.submit(() -> {
                    start.await();
                    return schemas.add(loader.getSchema());
                });
            }
            start.countDown();
        } finally {
            executor.shutdown();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(schemas).hasSize(1);
    }

    private static GraphQlSchemaLoader loader(String... files) {
        return new GraphQlSchemaLoader(java.util.Arrays.stream(files).map(file -> SCHEMA_DIR + file).toList());
    }
}
