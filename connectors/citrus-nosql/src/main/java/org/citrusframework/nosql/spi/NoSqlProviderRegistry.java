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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.spi.ResourcePathTypeResolver;

/**
 * Immutable registry of NoSQL providers discovered from the classpath.
 *
 * <p>Duplicate provider ids, blank ids and unknown ids are configuration errors. Capabilities and
 * native commands are checked before the provider is called, so unsupported operations fail
 * deterministically without touching a database.</p>
 */
public class NoSqlProviderRegistry {

    /**
     * Classpath resource path for provider registrations. Each resource declares the provider
     * implementation with a {@code type} property.
     */
    public static final String RESOURCE_PATH = "META-INF/citrus/nosql/provider";

    private static final ResourcePathTypeResolver RESOLVER =
            new ResourcePathTypeResolver(RESOURCE_PATH, NoSqlProvider.class);

    private static volatile NoSqlProviderRegistry cachedRegistry;

    private final Map<String, NoSqlProvider> providers;

    public NoSqlProviderRegistry(Collection<NoSqlProvider> providers) {
        Objects.requireNonNull(providers, "Providers must not be null");

        Map<String, NoSqlProvider> registered = new LinkedHashMap<>();
        for (NoSqlProvider provider : providers) {
            Objects.requireNonNull(provider, "Provider must not be null");

            String providerId = provider.id();
            if (providerId == null || providerId.isBlank()) {
                throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                        String.format("NoSQL provider '%s' has no id", provider.getClass().getName()));
            }

            NoSqlProvider previous = registered.putIfAbsent(providerId, provider);
            if (previous != null) {
                throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                        String.format("Duplicate NoSQL provider id '%s' registered by '%s' and '%s'",
                                providerId, previous.getClass().getName(), provider.getClass().getName()));
            }
        }

        this.providers = Collections.unmodifiableMap(registered);
    }

    /**
     * Discover all providers on the classpath. The result is cached because providers are stateless
     * and thread-safe, so a single instance serves all operations of a test suite.
     */
    public static NoSqlProviderRegistry lookup() {
        NoSqlProviderRegistry current = cachedRegistry;
        if (current == null) {
            synchronized (NoSqlProviderRegistry.class) {
                current = cachedRegistry;
                if (current == null) {
                    Map<String, NoSqlProvider> resolved = RESOLVER.resolveAll();
                    current = new NoSqlProviderRegistry(resolved.values());
                    cachedRegistry = current;
                }
            }
        }

        return current;
    }

    public static NoSqlProviderRegistry of(NoSqlProvider... providers) {
        return new NoSqlProviderRegistry(java.util.Arrays.asList(providers));
    }

    /**
     * Resolve a provider by id.
     */
    public NoSqlProvider resolve(String providerId) {
        NoSqlProvider provider = providers.get(providerId);
        if (provider == null) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    String.format("Unknown NoSQL provider '%s'. Available providers: %s",
                            providerId, providers.keySet()));
        }

        return provider;
    }

    /**
     * Check that the provider supports the requested kind and native command.
     */
    public void checkSupport(NoSqlProvider provider, NoSqlOperationKind kind, String nativeCommand) {
        if (!provider.capabilities().contains(kind.capability())) {
            throw new NoSqlException(NoSqlErrorClass.UNSUPPORTED_OPERATION,
                    String.format("NoSQL provider '%s' does not support operation '%s'", provider.id(), kind));
        }

        if (kind == NoSqlOperationKind.NATIVE) {
            if (nativeCommand == null || nativeCommand.isBlank()) {
                throw new NoSqlException(NoSqlErrorClass.INVALID_PAYLOAD,
                        String.format("NoSQL provider '%s' requires a native command name", provider.id()));
            }

            if (!provider.nativeCommands().contains(nativeCommand)) {
                throw new NoSqlException(NoSqlErrorClass.UNSUPPORTED_OPERATION,
                        String.format("NoSQL provider '%s' does not support native command '%s'. Supported commands: %s",
                                provider.id(), nativeCommand, provider.nativeCommands()));
            }
        }
    }

    public Collection<NoSqlProvider> providers() {
        return providers.values();
    }
}
