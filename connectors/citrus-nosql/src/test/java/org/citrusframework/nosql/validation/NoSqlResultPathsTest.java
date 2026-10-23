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

package org.citrusframework.nosql.validation;

import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.nosql.actions.NoSqlExtraction;
import org.citrusframework.nosql.actions.NoSqlValidation;
import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.nosql.model.NativeResult;
import org.citrusframework.nosql.model.NoSqlValue;
import org.citrusframework.nosql.model.QueryResult;
import org.citrusframework.nosql.model.ScalarResult;
import org.citrusframework.nosql.model.WriteResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class NoSqlResultPathsTest {

    private TestContext context;

    @BeforeMethod
    public void prepareTest() {
        context = TestContextFactory.newInstance().getObject();
    }

    @Test
    public void shouldEvaluateDocumentPaths() {
        QueryResult result = QueryResult.of(List.of(
                NoSqlValue.object(Map.of("orderId", NoSqlValue.string("123"),
                        "status", NoSqlValue.string("COMPLETED")))));

        assertEquals(NoSqlResultPaths.evaluate(result, "$.documents[0].status"),
                NoSqlValue.string("COMPLETED"));
        assertEquals(NoSqlResultPaths.evaluate(result, "$.documents[0].orderId"),
                NoSqlValue.string("123"));
        assertEquals(NoSqlResultPaths.evaluate(result, "$.documents.length()"),
                NoSqlValue.number(1));
    }

    @Test
    public void shouldEvaluateTotalCountOnlyWhenReported() {
        QueryResult withCount = QueryResult.of(List.of(), 42);
        QueryResult withoutCount = QueryResult.of(List.of());

        assertEquals(NoSqlResultPaths.evaluate(withCount, "$.totalCount"), NoSqlValue.number(42));
        assertTrue(NoSqlResultPaths.evaluate(withoutCount, "$.totalCount").isMissing());
    }

    @Test
    public void shouldKeepAbsentWriteCountersMissing() {
        WriteResult result = WriteResult.inserted(1, List.of(NoSqlValue.string("abc")));

        assertEquals(NoSqlResultPaths.evaluate(result, "$.inserted"), NoSqlValue.number(1));
        assertEquals(NoSqlResultPaths.evaluate(result, "$.generatedIds[0]"), NoSqlValue.string("abc"));
        assertTrue(NoSqlResultPaths.evaluate(result, "$.matched").isMissing());
        assertTrue(NoSqlResultPaths.evaluate(result, "$.deleted").isMissing());
    }

    @Test
    public void shouldDistinguishNullFromMissing() {
        QueryResult result = QueryResult.of(List.of(
                NoSqlValue.object(Map.of("note", NoSqlValue.nullValue()))));

        NoSqlValue presentNull = NoSqlResultPaths.evaluate(result, "$.documents[0].note");
        assertTrue(presentNull.isNull());
        assertEquals(presentNull.render(), "null");

        NoSqlValue absent = NoSqlResultPaths.evaluate(result, "$.documents[0].unknown");
        assertTrue(absent.isMissing());
    }

    @Test
    public void shouldValidateValuesAndMatchers() {
        QueryResult result = QueryResult.of(List.of(
                NoSqlValue.object(Map.of("status", NoSqlValue.string("COMPLETED")))));

        NoSqlResultPaths.validate(result, List.of(
                new NoSqlValidation("$.documents[0].status", "COMPLETED"),
                new NoSqlValidation("$.documents[0].status", "@startsWith('COMP')@")), context);
    }

    @Test
    public void shouldFailValidationOnMismatch() {
        QueryResult result = QueryResult.of(List.of(
                NoSqlValue.object(Map.of("status", NoSqlValue.string("PENDING")))));

        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> NoSqlResultPaths.validate(result,
                        List.of(new NoSqlValidation("$.documents[0].status", "COMPLETED")), context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.VALIDATION);
        assertTrue(exception.getMessage().contains("PENDING"));
    }

    @Test
    public void shouldWrapMatcherFailureAsValidationError() {
        QueryResult result = QueryResult.of(List.of(
                NoSqlValue.object(Map.of("status", NoSqlValue.string("PENDING")))));

        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> NoSqlResultPaths.validate(result,
                        List.of(new NoSqlValidation("$.documents[0].status", "@startsWith('COMP')@")), context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.VALIDATION);
    }

    @Test
    public void shouldTreatEmptyIndefinitePathResultAsMissing() {
        QueryResult result = QueryResult.of(List.of(
                NoSqlValue.object(Map.of("status", NoSqlValue.string("PENDING")))));

        assertTrue(NoSqlResultPaths.evaluate(result, "$.documents[?(@.status == 'COMPLETED')]").isMissing());
        assertEquals(NoSqlResultPaths.evaluate(result, "$.documents[?(@.status == 'PENDING')].status"),
                NoSqlValue.array(NoSqlValue.string("PENDING")));
    }

    @Test
    public void shouldFailValidationOnMissingPath() {
        QueryResult result = QueryResult.of(List.of());

        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> NoSqlResultPaths.validate(result,
                        List.of(new NoSqlValidation("$.documents[0].status", "COMPLETED")), context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.VALIDATION);
    }

    @Test
    public void shouldExtractValuesAsText() {
        QueryResult result = QueryResult.of(List.of(
                NoSqlValue.object(Map.of("customer", NoSqlValue.object(Map.of(
                        "id", NoSqlValue.string("customer-42")))))));

        NoSqlResultPaths.extract(result, List.of(
                new NoSqlExtraction("$.documents[0].customer.id", "customerId"),
                new NoSqlExtraction("$.documents[0].customer", "customer")), context);

        assertEquals(context.getVariable("customerId"), "customer-42");
        assertEquals(context.getVariable("customer"), "{\"id\":\"customer-42\"}");
    }

    @Test
    public void shouldFailExtractionOnMissingPath() {
        QueryResult result = QueryResult.of(List.of());

        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> NoSqlResultPaths.extract(result,
                        List.of(new NoSqlExtraction("$.documents[0].customer.id", "customerId")), context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.EXTRACTION);
    }

    @Test
    public void shouldEvaluateScalarAndNativeResults() {
        ScalarResult scalar = new ScalarResult(NoSqlValue.number(7));
        assertEquals(NoSqlResultPaths.evaluate(scalar, "$.value"), NoSqlValue.number(7));

        NativeResult nativeResult = NativeResult.of("mongodb", NoSqlValue.array(List.of(
                NoSqlValue.object(Map.of("count", NoSqlValue.number(2))))));
        assertEquals(NoSqlResultPaths.evaluate(nativeResult, "$.providerId"), NoSqlValue.string("mongodb"));
        assertEquals(NoSqlResultPaths.evaluate(nativeResult, "$.value[0].count"), NoSqlValue.number(2));
    }
}
