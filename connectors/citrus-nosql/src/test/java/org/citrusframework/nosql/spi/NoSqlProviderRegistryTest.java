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

package org.citrusframework.nosql.spi;

import java.util.List;
import java.util.Set;

import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.nosql.model.NoSqlResult;
import org.citrusframework.nosql.model.QueryResult;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class NoSqlProviderRegistryTest {

    @Test
    public void shouldResolveProviderById() {
        TestProvider provider = new TestProvider("mongodb", Set.of(NoSqlCapability.QUERY), Set.of());

        NoSqlProviderRegistry registry = NoSqlProviderRegistry.of(provider);

        assertSame(registry.resolve("mongodb"), provider);
        assertEquals(registry.providers().size(), 1);
        assertTrue(registry.providers().contains(provider));
    }

    @Test
    public void shouldRejectDuplicateProviderIds() {
        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> NoSqlProviderRegistry.of(
                        new TestProvider("mongodb", Set.of(NoSqlCapability.QUERY), Set.of()),
                        new TestProvider("mongodb", Set.of(NoSqlCapability.QUERY), Set.of())));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
        assertTrue(exception.getMessage().contains("Duplicate NoSQL provider id 'mongodb'"));
    }

    @Test
    public void shouldRejectBlankProviderId() {
        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> NoSqlProviderRegistry.of(new TestProvider(" ", Set.of(), Set.of())));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
    }

    @Test
    public void shouldRejectUnknownProvider() {
        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> NoSqlProviderRegistry.of().resolve("redis"));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
        assertTrue(exception.getMessage().contains("Unknown NoSQL provider 'redis'"));
    }

    @Test
    public void shouldRejectUnsupportedOperationKind() {
        TestProvider provider = new TestProvider("mongodb", Set.of(NoSqlCapability.QUERY), Set.of());
        NoSqlProviderRegistry registry = NoSqlProviderRegistry.of(provider);

        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> registry.checkSupport(provider, NoSqlOperationKind.INSERT, null));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.UNSUPPORTED_OPERATION);
    }

    @Test
    public void shouldRejectUndeclaredNativeCommand() {
        TestProvider provider = new TestProvider("mongodb", Set.of(NoSqlCapability.NATIVE), Set.of("mongodb.aggregate"));
        NoSqlProviderRegistry registry = NoSqlProviderRegistry.of(provider);

        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> registry.checkSupport(provider, NoSqlOperationKind.NATIVE, "mongodb.mapReduce"));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.UNSUPPORTED_OPERATION);
        assertTrue(exception.getMessage().contains("mongodb.mapReduce"));
    }

    @Test
    public void shouldRejectMissingNativeCommandName() {
        TestProvider provider = new TestProvider("mongodb", Set.of(NoSqlCapability.NATIVE), Set.of("mongodb.aggregate"));
        NoSqlProviderRegistry registry = NoSqlProviderRegistry.of(provider);

        NoSqlException exception = expectThrows(NoSqlException.class,
                () -> registry.checkSupport(provider, NoSqlOperationKind.NATIVE, " "));

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.INVALID_PAYLOAD);
    }

    @Test
    public void shouldAcceptDeclaredNativeCommand() {
        TestProvider provider = new TestProvider("mongodb", Set.of(NoSqlCapability.NATIVE), Set.of("mongodb.aggregate"));
        NoSqlProviderRegistry registry = NoSqlProviderRegistry.of(provider);

        registry.checkSupport(provider, NoSqlOperationKind.NATIVE, "mongodb.aggregate");
    }

    private record TestProvider(String id, Set<NoSqlCapability> capabilities,
                                Set<String> nativeCommands) implements NoSqlProvider {

        @Override
            public NoSqlResult execute(NoSqlOperation operation, NoSqlProviderContext context) {
                return QueryResult.of(List.of());
            }
        }
}
