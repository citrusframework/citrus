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

package org.citrusframework.nosql.actions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.nosql.config.NoSqlDatabaseConfiguration;
import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.nosql.model.NoSqlResult;
import org.citrusframework.nosql.model.NoSqlValue;
import org.citrusframework.nosql.model.QueryResult;
import org.citrusframework.nosql.model.WriteResult;
import org.citrusframework.nosql.spi.NoSqlCapability;
import org.citrusframework.nosql.spi.NoSqlOperation;
import org.citrusframework.nosql.spi.NoSqlOperationKind;
import org.citrusframework.nosql.spi.NoSqlProvider;
import org.citrusframework.nosql.spi.NoSqlProviderContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class NoSqlActionTest {

    private TestContext context;
    private TestProvider provider;

    @BeforeMethod
    public void prepareTest() {
        context = TestContextFactory.newInstance().getObject();
        provider = new TestProvider();
        context.getReferenceResolver().bind("ordersDatabase", NoSqlDatabaseConfiguration.builder("ordersDatabase")
                .provider(provider.id())
                .database("orders")
                .client(new Object())
                .build());
    }

    @Test
    public void shouldExecuteQueryWithValidationAndExtraction() {
        context.setVariable("orderId", "123");
        provider.result = QueryResult.of(List.of(NoSqlValue.object(Map.of(
                "orderId", NoSqlValue.string("123"),
                "status", NoSqlValue.string("COMPLETED"),
                "customerId", NoSqlValue.string("customer-42")))));

        action()
                .query()
                .target("orders")
                .filter("{\"orderId\": \"${orderId}\"}")
                .validate("$.documents[0].status", "COMPLETED")
                .extract("$.documents[0].customerId", "customerId")
                .maxResults(10)
                .build()
                .execute(context);

        assertEquals(context.getVariable("customerId"), "customer-42");
        assertEquals(provider.executions.size(), 1);

        NoSqlOperation operation = provider.executions.get(0);
        assertEquals(operation.kind(), NoSqlOperationKind.QUERY);
        assertEquals(operation.target(), "orders");
        assertEquals(operation.payload().content(), "{\"orderId\": \"123\"}");
        assertEquals(operation.options().maxResults(), java.util.OptionalInt.of(10));
    }

    @Test
    public void shouldFailOnUnknownConfiguration() {
        NoSqlAction action = new NoSqlAction.Builder()
                .client("unknownDatabase")
                .query()
                .target("orders")
                .provider(provider)
                .build();

        NoSqlException exception = expectThrows(NoSqlException.class, () -> action.execute(context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
        assertTrue(provider.executions.isEmpty());
    }

    @Test
    public void shouldRejectUnsupportedOperationBeforeProviderCall() {
        provider.capabilities = Set.of(NoSqlCapability.QUERY);

        NoSqlAction action = action().insert().target("orders").payload("{}").build();

        NoSqlException exception = expectThrows(NoSqlException.class, () -> action.execute(context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.UNSUPPORTED_OPERATION);
        assertTrue(provider.executions.isEmpty());
    }

    @Test
    public void shouldRejectUndeclaredNativeCommandBeforeProviderCall() {
        NoSqlAction action = action().nativeOperation("test.mapReduce").target("orders").payload("{}").build();

        NoSqlException exception = expectThrows(NoSqlException.class, () -> action.execute(context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.UNSUPPORTED_OPERATION);
        assertTrue(provider.executions.isEmpty());
    }

    @Test
    public void shouldRequirePayloadForWritesAndNativeOperations() {
        NoSqlAction action = action().insert().target("orders").build();

        NoSqlException exception = expectThrows(NoSqlException.class, () -> action.execute(context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.INVALID_PAYLOAD);
        assertTrue(provider.executions.isEmpty());
    }

    @Test
    public void shouldReportUnknownOutcomeForWriteTimeoutWithoutRetry() {
        provider.failure = NoSqlException.writeTimeout("Timed out", null);

        NoSqlAction action = action().insert().target("orders").payload("{}")
                .retry(3, Duration.ZERO).build();

        NoSqlException exception = expectThrows(NoSqlException.class, () -> action.execute(context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.TIMEOUT);
        assertTrue(exception.isWriteOutcomeUnknown());
        assertEquals(provider.executions.size(), 1);
    }

    @Test
    public void shouldRetryReadsOnConnectionFailure() {
        provider.failures = List.of(new NoSqlException(NoSqlErrorClass.CONNECTION, "Connection reset"));
        provider.result = QueryResult.of(List.of());

        action().query().target("orders").retry(2, Duration.ZERO).build().execute(context);

        assertEquals(provider.executions.size(), 2);
    }

    @Test
    public void shouldExhaustReadRetries() {
        provider.failure = new NoSqlException(NoSqlErrorClass.CONNECTION, "Connection reset");

        NoSqlAction action = action().query().target("orders").retry(2, Duration.ZERO).build();

        NoSqlException exception = expectThrows(NoSqlException.class, () -> action.execute(context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONNECTION);
        assertEquals(provider.executions.size(), 2);
    }

    @Test
    public void shouldNotRetryWrites() {
        provider.failure = new NoSqlException(NoSqlErrorClass.CONNECTION, "Connection reset");

        NoSqlAction action = action().insert().target("orders").payload("{}")
                .retry(3, Duration.ZERO).build();

        NoSqlException exception = expectThrows(NoSqlException.class, () -> action.execute(context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONNECTION);
        assertEquals(provider.executions.size(), 1);
    }

    @Test
    public void shouldPropagateValidationFailure() {
        provider.result = WriteResult.deleted(0);

        NoSqlAction action = action().delete().target("orders").payload("{}")
                .validate("$.deleted", "1").build();

        NoSqlException exception = expectThrows(NoSqlException.class, () -> action.execute(context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.VALIDATION);
    }

    @Test
    public void shouldNotSubstituteTargetOrCommandExpressions() {
        context.setVariable("target", "customers");

        action().nativeOperation("test.command").target("orders").payload("{}").build().execute(context);

        assertEquals(provider.executions.get(0).target(), "orders");
    }

    @Test
    public void shouldNotMutateSpecOnSubstitution() {
        context.setVariable("orderId", "123");
        context.setVariable("expected", "COMPLETED");
        NoSqlActionSpec spec = new NoSqlActionSpec("ordersDatabase", NoSqlOperationKind.QUERY, null, "orders",
                org.citrusframework.nosql.spi.NoSqlPayload.text("{\"orderId\": \"${orderId}\"}"),
                List.of(new NoSqlValidation("$.documents[0].status", "${expected}")),
                List.of(), java.util.OptionalInt.empty(), NoSqlRetryPolicy.none());

        NoSqlActionSpec substituted = spec.substitute(context);

        assertEquals(spec.payload().content(), "{\"orderId\": \"${orderId}\"}");
        assertEquals(substituted.payload().content(), "{\"orderId\": \"123\"}");
        assertEquals(spec.validations().get(0).expected(), "${expected}");
        assertEquals(substituted.validations().get(0).expected(), "COMPLETED");
    }

    @Test
    public void shouldRejectNullProviderResult() {
        provider.result = null;

        NoSqlAction action = action().query().target("orders").build();

        NoSqlException exception = expectThrows(NoSqlException.class, () -> action.execute(context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.EXECUTION);
    }

    @Test
    public void shouldRejectInvalidValidationExtractionAndRetryDefinitions() {
        NoSqlException blankValidationPath = expectThrows(NoSqlException.class,
                () -> new NoSqlValidation(" ", "COMPLETED"));
        assertEquals(blankValidationPath.getErrorClass(), NoSqlErrorClass.CONFIGURATION);

        NoSqlException blankExtractionPath = expectThrows(NoSqlException.class,
                () -> new NoSqlExtraction(" ", "var"));
        assertEquals(blankExtractionPath.getErrorClass(), NoSqlErrorClass.CONFIGURATION);

        NoSqlException blankVariable = expectThrows(NoSqlException.class,
                () -> new NoSqlExtraction("$.value", " "));
        assertEquals(blankVariable.getErrorClass(), NoSqlErrorClass.CONFIGURATION);

        NoSqlException zeroAttempts = expectThrows(NoSqlException.class,
                () -> NoSqlRetryPolicy.of(0, Duration.ZERO));
        assertEquals(zeroAttempts.getErrorClass(), NoSqlErrorClass.CONFIGURATION);

        NoSqlException badInterval = expectThrows(NoSqlException.class,
                () -> NoSqlRetryPolicy.of(2, "not-a-duration"));
        assertEquals(badInterval.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
    }

    private NoSqlAction.Builder action() {
        return new NoSqlAction.Builder()
                .client("ordersDatabase")
                .provider(provider);
    }

    private static final class TestProvider implements NoSqlProvider {

        private final List<NoSqlOperation> executions = new ArrayList<>();
        private List<NoSqlException> failures = List.of();
        private NoSqlException failure;
        private NoSqlResult result = QueryResult.of(List.of());
        private Set<NoSqlCapability> capabilities = Set.of(NoSqlCapability.values());

        @Override
        public String id() {
            return "test";
        }

        @Override
        public Set<NoSqlCapability> capabilities() {
            return capabilities;
        }

        @Override
        public Set<String> nativeCommands() {
            return Set.of("test.command");
        }

        @Override
        public NoSqlResult execute(NoSqlOperation operation, NoSqlProviderContext providerContext) {
            executions.add(operation);

            if (!failures.isEmpty() && executions.size() <= failures.size()) {
                throw failures.get(executions.size() - 1);
            }

            if (failure != null) {
                throw failure;
            }

            return result;
        }
    }
}
