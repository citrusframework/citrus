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

package org.citrusframework.nosql.config;

import java.util.concurrent.atomic.AtomicInteger;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class NoSqlDatabaseConfigurationsTest {

    private TestContext context;

    @BeforeMethod
    public void prepareTest() {
        context = TestContextFactory.newInstance().getObject();
    }

    @Test
    public void shouldResolveConfigurationDirectlyByName() {
        NoSqlDatabaseConfiguration configuration = configuration("ordersDatabase");

        context.getReferenceResolver().bind("ordersDatabase", configuration);

        assertSame(NoSqlDatabaseConfigurations.resolve(context, "ordersDatabase"), configuration);
    }

    @Test
    public void shouldRegisterConfigurationUnderItsOwnName() {
        NoSqlDatabaseConfiguration configuration = configuration("ordersDatabase");

        NoSqlDatabaseConfigurations.register(context, configuration);

        assertSame(NoSqlDatabaseConfigurations.resolve(context, "ordersDatabase"), configuration);
    }

    @Test
    public void shouldRejectRegistrationWithoutConfiguration() {
        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> NoSqlDatabaseConfigurations.register(context, null));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
    }

    @Test
    public void shouldResolveConfigurationFromCollection() {
        NoSqlDatabaseConfiguration configuration = configuration("ordersDatabase");

        context.getReferenceResolver().bind(NoSqlDatabaseConfigurations.REFERENCE_NAME,
                NoSqlDatabaseConfigurations.of(configuration));

        assertSame(NoSqlDatabaseConfigurations.resolve(context, "ordersDatabase"), configuration);
    }

    @Test
    public void shouldRejectUnknownConfiguration() {
        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> NoSqlDatabaseConfigurations.resolve(context, "unknown"));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
        assertTrue(exception.getMessage().contains("unknown"));
    }

    @Test
    public void shouldRejectBlankConfigurationName() {
        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> NoSqlDatabaseConfigurations.resolve(context, " "));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
    }

    @Test
    public void shouldRejectDuplicateConfigurationNames() {
        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> NoSqlDatabaseConfigurations.of(
                        configuration("ordersDatabase"),
                        configuration("ordersDatabase")));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
        assertTrue(exception.getMessage().contains("Duplicate NoSQL database configuration"));
    }

    @Test
    public void shouldRequireProviderAndClient() {
        NoSqlException missingProvider = expectThrows(NoSqlException.class,
                () -> NoSqlDatabaseConfiguration.builder("ordersDatabase").client(new Object()).build());
        assertEquals(missingProvider.getErrorClass(), NoSqlErrorClass.CONFIGURATION);

        NoSqlException missingClient = expectThrows(NoSqlException.class,
                () -> NoSqlDatabaseConfiguration.builder("ordersDatabase").provider("mongodb").build());
        assertEquals(missingClient.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
    }

    @Test
    public void shouldNeverRenderClientDetails() {
        NoSqlDatabaseConfiguration configuration = NoSqlDatabaseConfiguration.builder("ordersDatabase")
                .provider("mongodb")
                .database("orders")
                .client("mongodb://user:secret@localhost:27017")
                .build();

        assertFalse(configuration.toString().contains("secret"));
        assertTrue(configuration.toString().contains("ordersDatabase"));
    }

    @Test
    public void shouldCloseClientOnDestroyExactlyOnce() {
        AtomicInteger closed = new AtomicInteger();
        NoSqlDatabaseConfiguration configuration = NoSqlDatabaseConfiguration.builder("ordersDatabase")
                .provider("mongodb")
                .database("orders")
                .client((AutoCloseable) closed::incrementAndGet)
                .build();

        configuration.destroy();
        configuration.destroy();
        configuration.close();

        assertEquals(closed.get(), 1);
    }

    @Test
    public void shouldCloseBoundClientWhenResolverIsDestroyed() {
        AtomicInteger closed = new AtomicInteger();
        NoSqlDatabaseConfiguration configuration = NoSqlDatabaseConfiguration.builder("ordersDatabase")
                .provider("mongodb")
                .database("orders")
                .client((AutoCloseable) closed::incrementAndGet)
                .build();

        context.getReferenceResolver().bind("ordersDatabase", configuration);
        context.getReferenceResolver().destroy();

        assertEquals(closed.get(), 1);
    }

    @Test
    public void shouldCloseClientsInBoundCollectionWhenResolverIsDestroyed() {
        AtomicInteger closed = new AtomicInteger();
        NoSqlDatabaseConfiguration orders = NoSqlDatabaseConfiguration.builder("ordersDatabase")
                .provider("mongodb")
                .database("orders")
                .client((AutoCloseable) closed::incrementAndGet)
                .build();
        NoSqlDatabaseConfiguration customers = NoSqlDatabaseConfiguration.builder("customersDatabase")
                .provider("mongodb")
                .database("customers")
                .client((AutoCloseable) closed::incrementAndGet)
                .build();

        context.getReferenceResolver().bind(NoSqlDatabaseConfigurations.REFERENCE_NAME,
                NoSqlDatabaseConfigurations.of(orders, customers));
        context.getReferenceResolver().destroy();

        assertEquals(closed.get(), 2);
    }

    @Test
    public void shouldIgnoreDestroyForNonCloseableClient() {
        configuration("ordersDatabase").destroy();
    }

    private static NoSqlDatabaseConfiguration configuration(String name) {
        return NoSqlDatabaseConfiguration.builder(name)
                .provider("mongodb")
                .database("orders")
                .client(new Object())
                .build();
    }
}
