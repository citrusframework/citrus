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

package org.citrusframework.nosql;

import java.time.Duration;
import java.util.List;

import org.citrusframework.api.actions.nosql.NoSqlActionBuilder;
import org.citrusframework.dsl.nosql.NoSqlTestActionSupport;
import org.citrusframework.nosql.actions.NoSqlAction;
import org.citrusframework.nosql.actions.NoSqlActionSpec;
import org.citrusframework.nosql.actions.NoSqlExtraction;
import org.citrusframework.nosql.actions.NoSqlValidation;
import org.citrusframework.nosql.spi.NoSqlOperationKind;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Cross DSL parity: the Java, XML and YAML surfaces must produce the same action specification.
 */
public class NoSqlDslParityTest implements NoSqlTestActionSupport {

    @Test
    public void shouldProduceSameSpecFromAllDsls() {
        NoSqlActionSpec javaSpec = javaSpec();
        NoSqlActionSpec xmlSpec = xmlSpec();
        NoSqlActionSpec yamlSpec = yamlSpec();

        assertSameSpec(javaSpec, xmlSpec);
        assertSameSpec(javaSpec, yamlSpec);
    }

    private NoSqlActionSpec javaSpec() {
        NoSqlActionBuilder<?, ?> builder = nosql()
                .client("ordersDatabase")
                .query()
                .target("orders")
                .filter("{ \"orderId\": \"${orderId}\" }")
                .validate("$.documents[0].status", "COMPLETED")
                .extract("$.documents[0].customerId", "customerId")
                .maxResults(10)
                .retry(3, Duration.ofMillis(500));

        return ((NoSqlAction) builder.build()).getSpec();
    }

    private NoSqlActionSpec xmlSpec() {
        org.citrusframework.nosql.xml.NoSql noSql = new org.citrusframework.nosql.xml.NoSql();
        noSql.setClient("ordersDatabase");
        noSql.setOperation("query");
        noSql.setTarget("orders");
        noSql.setFilter("{ \"orderId\": \"${orderId}\" }");
        noSql.setMaxResults(10);
        noSql.setRetryAttempts(3);
        noSql.setRetryInterval("PT0.5S");
        noSql.setValidations(List.of(validate("$.documents[0].status", "COMPLETED")));
        noSql.setExtractions(List.of(extract("$.documents[0].customerId", "customerId")));

        return noSql.build().getSpec();
    }

    private NoSqlActionSpec yamlSpec() {
        org.citrusframework.nosql.yaml.NoSql noSql = new org.citrusframework.nosql.yaml.NoSql();
        noSql.setClient("ordersDatabase");
        noSql.setOperation("query");
        noSql.setTarget("orders");
        noSql.setFilter("{ \"orderId\": \"${orderId}\" }");
        noSql.setMaxResults(10);
        noSql.setRetryAttempts(3);
        noSql.setRetryInterval("500");
        noSql.setValidate(List.of(yamlValidate("$.documents[0].status", "COMPLETED")));
        noSql.setExtract(List.of(yamlExtract("$.documents[0].customerId", "customerId")));

        return noSql.build().getSpec();
    }

    private static void assertSameSpec(NoSqlActionSpec expected, NoSqlActionSpec actual) {
        assertEquals(actual.client(), expected.client());
        assertEquals(actual.kind(), expected.kind());
        assertEquals(actual.nativeCommand(), expected.nativeCommand());
        assertEquals(actual.target(), expected.target());
        assertEquals(actual.payload(), expected.payload());
        assertEquals(actual.validations(), expected.validations());
        assertEquals(actual.extractions(), expected.extractions());
        assertEquals(actual.maxResults(), expected.maxResults());
        assertEquals(actual.retryPolicy(), expected.retryPolicy());
    }

    private static org.citrusframework.nosql.xml.NoSql.Validate validate(String path, String value) {
        org.citrusframework.nosql.xml.NoSql.Validate validate = new org.citrusframework.nosql.xml.NoSql.Validate();
        validate.setPath(path);
        validate.setValue(value);
        return validate;
    }

    private static org.citrusframework.nosql.xml.NoSql.Extract extract(String path, String variable) {
        org.citrusframework.nosql.xml.NoSql.Extract extract = new org.citrusframework.nosql.xml.NoSql.Extract();
        extract.setPath(path);
        extract.setVariable(variable);
        return extract;
    }

    private static org.citrusframework.nosql.yaml.NoSql.Validate yamlValidate(String path, String value) {
        org.citrusframework.nosql.yaml.NoSql.Validate validate = new org.citrusframework.nosql.yaml.NoSql.Validate();
        validate.setPath(path);
        validate.setValue(value);
        return validate;
    }

    private static org.citrusframework.nosql.yaml.NoSql.Extract yamlExtract(String path, String variable) {
        org.citrusframework.nosql.yaml.NoSql.Extract extract = new org.citrusframework.nosql.yaml.NoSql.Extract();
        extract.setPath(path);
        extract.setVariable(variable);
        return extract;
    }

    @Test
    public void shouldProduceSameNativeSpecFromAllDsls() {
        NoSqlActionSpec javaSpec = ((NoSqlAction) nosql()
                .client("ordersDatabase")
                .nativeOperation("mongodb.aggregate")
                .target("orders")
                .payload("[{ \"$match\": {} }]")
                .build()).getSpec();

        org.citrusframework.nosql.xml.NoSql xml = new org.citrusframework.nosql.xml.NoSql();
        xml.setClient("ordersDatabase");
        xml.setOperation("native");
        xml.setCommand("mongodb.aggregate");
        xml.setTarget("orders");
        xml.setPayload("[{ \"$match\": {} }]");

        org.citrusframework.nosql.yaml.NoSql yaml = new org.citrusframework.nosql.yaml.NoSql();
        yaml.setClient("ordersDatabase");
        yaml.setOperation("native");
        yaml.setCommand("mongodb.aggregate");
        yaml.setTarget("orders");
        yaml.setPayload("[{ \"$match\": {} }]");

        assertSameSpec(javaSpec, xml.build().getSpec());
        assertSameSpec(javaSpec, yaml.build().getSpec());
    }

    @Test
    public void shouldUseTheSameOperationNamesEverywhere() {
        for (String operation : List.of("query", "insert", "update", "delete")) {
            org.citrusframework.nosql.xml.NoSql xml = new org.citrusframework.nosql.xml.NoSql();
            xml.setClient("ordersDatabase");
            xml.setOperation(operation);
            xml.setTarget("orders");

            org.citrusframework.nosql.yaml.NoSql yaml = new org.citrusframework.nosql.yaml.NoSql();
            yaml.setClient("ordersDatabase");
            yaml.setOperation(operation);
            yaml.setTarget("orders");

            assertEquals(xml.build().getSpec().kind(), yaml.build().getSpec().kind());
        }
    }

    @Test
    public void shouldExposeValidationAndExtractionTypesToAllDsls() {
        NoSqlValidation validation = new NoSqlValidation("$.documents[0].status", "COMPLETED");
        NoSqlExtraction extraction = new NoSqlExtraction("$.documents[0].customerId", "customerId");

        org.citrusframework.nosql.xml.NoSql xml = new org.citrusframework.nosql.xml.NoSql();
        xml.setClient("ordersDatabase");
        xml.setOperation("query");
        xml.setTarget("orders");
        xml.setValidations(List.of(validate(validation.path(), validation.expected())));
        xml.setExtractions(List.of(extract(extraction.path(), extraction.variable())));

        org.citrusframework.nosql.yaml.NoSql yaml = new org.citrusframework.nosql.yaml.NoSql();
        yaml.setClient("ordersDatabase");
        yaml.setOperation("query");
        yaml.setTarget("orders");
        yaml.setValidate(List.of(yamlValidate(validation.path(), validation.expected())));
        yaml.setExtract(List.of(yamlExtract(extraction.path(), extraction.variable())));

        assertEquals(xml.build().getSpec().validations(), List.of(validation));
        assertEquals(xml.build().getSpec().extractions(), List.of(extraction));
        assertEquals(yaml.build().getSpec().validations(), List.of(validation));
        assertEquals(yaml.build().getSpec().extractions(), List.of(extraction));
    }

    @Test
    public void shouldKeepOperationKindsStable() {
        assertEquals(NoSqlOperationKind.QUERY.name().toLowerCase(), "query");
        assertEquals(NoSqlOperationKind.INSERT.name().toLowerCase(), "insert");
        assertEquals(NoSqlOperationKind.UPDATE.name().toLowerCase(), "update");
        assertEquals(NoSqlOperationKind.DELETE.name().toLowerCase(), "delete");
        assertEquals(NoSqlOperationKind.NATIVE.name().toLowerCase(), "native");
    }
}
