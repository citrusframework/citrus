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

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.nosql.config.NoSqlDatabaseConfiguration;
import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.nosql.model.NoSqlResult;
import org.citrusframework.nosql.model.NoSqlValue;
import org.citrusframework.nosql.model.QueryResult;
import org.citrusframework.nosql.spi.NoSqlCapability;
import org.citrusframework.nosql.spi.NoSqlOperation;
import org.citrusframework.nosql.spi.NoSqlOperationKind;
import org.citrusframework.nosql.spi.NoSqlProvider;
import org.citrusframework.nosql.spi.NoSqlProviderContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Regression tests for the defects found in the second implementation review.
 */
public class NoSqlActionRegressionTest {

    private TestContext context;
    private RecordingProvider provider;

    @BeforeMethod
    public void prepareTest() {
        context = TestContextFactory.newInstance().getObject();
        provider = new RecordingProvider();
        context.getReferenceResolver().bind("ordersDatabase", NoSqlDatabaseConfiguration.builder("ordersDatabase")
                .provider(provider.id())
                .database("orders")
                .client(new Object())
                .build());
    }

    @Test
    public void shouldNotCloseConfigurationClientDuringExecution() {
        AtomicInteger closed = new AtomicInteger();
        context.getReferenceResolver().bind("ordersDatabase", NoSqlDatabaseConfiguration.builder("ordersDatabase")
                .provider(provider.id())
                .database("orders")
                .client((AutoCloseable) closed::incrementAndGet)
                .build());

        action().query().target("orders").build().execute(context);

        assertEquals(closed.get(), 0, "The action must not close the shared client");
    }

    @Test
    public void shouldRejectFilterAndPayloadCombination() {
        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> action().query().target("orders").filter("{}").payload("{}").build());

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
    }

    @Test
    public void shouldClearStaleNativeCommand() {
        NoSqlActionSpec spec = action().nativeOperation("test.command").query().target("orders").build().getSpec();

        assertNull(spec.nativeCommand());
    }

    @Test
    public void shouldApplyConfiguredDefaultMaxResults() {
        context.getReferenceResolver().bind("ordersDatabase", NoSqlDatabaseConfiguration.builder("ordersDatabase")
                .provider(provider.id())
                .database("orders")
                .client(new Object())
                .defaultOption("maxResults", NoSqlValue.number(5))
                .build());

        action().query().target("orders").build().execute(context);

        assertEquals(provider.operation.options().maxResults().orElse(-1), 5);
    }

    @Test
    public void shouldPreferActionMaxResultsOverConfiguredDefault() {
        context.getReferenceResolver().bind("ordersDatabase", NoSqlDatabaseConfiguration.builder("ordersDatabase")
                .provider(provider.id())
                .database("orders")
                .client(new Object())
                .defaultOption("maxResults", NoSqlValue.number(5))
                .build());

        action().query().target("orders").maxResults(2).build().execute(context);

        assertEquals(provider.operation.options().maxResults().orElse(-1), 2);
    }

    @Test
    public void shouldReportUnresolvedVariableAsInvalidPayload() {
        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> action().query().target("orders").filter("{\"id\": \"${unknown}\"}").build().execute(context));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.INVALID_PAYLOAD);
    }

    @Test
    public void shouldNotRenderClientOrPayloadInSpiToString() {
        NoSqlProviderContext providerContext = new NoSqlProviderContext("ordersDatabase", "orders",
                "mongodb://user:secret@localhost:27017");
        NoSqlOperation operation = new NoSqlOperation(NoSqlOperationKind.QUERY, null, "orders",
                org.citrusframework.nosql.spi.NoSqlPayload.text("{\"secret\": \"value\"}"),
                org.citrusframework.nosql.spi.NoSqlOperationOptions.defaults());

        assertFalse(providerContext.toString().contains("secret"));
        assertTrue(providerContext.toString().contains("ordersDatabase"));
        assertFalse(operation.toString().contains("secret"));
    }

    private NoSqlAction.Builder action() {
        return new NoSqlAction.Builder()
                .client("ordersDatabase")
                .provider(provider);
    }

    private static final class RecordingProvider implements NoSqlProvider {

        private NoSqlOperation operation;

        @Override
        public String id() {
            return "test";
        }

        @Override
        public Set<NoSqlCapability> capabilities() {
            return Set.of(NoSqlCapability.QUERY, NoSqlCapability.INSERT, NoSqlCapability.UPDATE,
                    NoSqlCapability.DELETE, NoSqlCapability.NATIVE);
        }

        @Override
        public NoSqlResult execute(NoSqlOperation operation, NoSqlProviderContext context) {
            this.operation = operation;
            return QueryResult.of(List.of());
        }
    }
}
