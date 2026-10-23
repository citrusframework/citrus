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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.citrusframework.api.common.ShutdownPhase;
import org.citrusframework.context.TestContext;
import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.spi.ReferenceResolver;

/**
 * Collection of named database configurations. Duplicate names are rejected because a name must
 * resolve to exactly one configuration.
 */
public final class NoSqlDatabaseConfigurations implements ShutdownPhase, AutoCloseable {

    /**
     * Well-known reference name for a configuration collection in a Citrus reference resolver.
     */
    public static final String REFERENCE_NAME = "nosqlDatabases";

    private final Map<String, NoSqlDatabaseConfiguration> configurations;

    public NoSqlDatabaseConfigurations(Iterable<NoSqlDatabaseConfiguration> configurations) {
        Objects.requireNonNull(configurations, "Configurations must not be null");

        Map<String, NoSqlDatabaseConfiguration> registered = new LinkedHashMap<>();
        for (NoSqlDatabaseConfiguration configuration : configurations) {
            Objects.requireNonNull(configuration, "Configuration must not be null");

            NoSqlDatabaseConfiguration previous = registered.putIfAbsent(configuration.name(), configuration);
            if (previous != null) {
                throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                        String.format("Duplicate NoSQL database configuration '%s'", configuration.name()));
            }
        }

        this.configurations = Collections.unmodifiableMap(registered);
    }

    public static NoSqlDatabaseConfigurations of(NoSqlDatabaseConfiguration... configurations) {
        return new NoSqlDatabaseConfigurations(Arrays.asList(configurations));
    }

    public Optional<NoSqlDatabaseConfiguration> find(String name) {
        return Optional.ofNullable(configurations.get(name));
    }

    public NoSqlDatabaseConfiguration require(String name) {
        return find(name).orElseThrow(() -> new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                String.format("Unknown NoSQL database configuration '%s'. Available configurations: %s",
                        name, configurations.keySet())));
    }

    public Map<String, NoSqlDatabaseConfiguration> configurations() {
        return configurations;
    }

    /**
     * Closes every configuration in the collection when the Citrus context shuts down.
     */
    @Override
    public void destroy() {
        configurations.values().forEach(NoSqlDatabaseConfiguration::destroy);
    }

    @Override
    public void close() {
        destroy();
    }

    /**
     * Register a configuration under its own name, so callers do not repeat the name string.
     */
    public static void register(TestContext context, NoSqlDatabaseConfiguration configuration) {
        if (context == null) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "Unable to register NoSQL database configuration: no test context available");
        }

        if (configuration == null) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "Unable to register NoSQL database configuration: configuration must not be null");
        }

        context.getReferenceResolver().bind(configuration.name(), configuration);
    }

    /**
     * Resolve a single named configuration from the current test context. The name may reference a
     * configuration directly or an entry of a {@link NoSqlDatabaseConfigurations} collection
     * registered under {@value #REFERENCE_NAME}.
     */
    public static NoSqlDatabaseConfiguration resolve(TestContext context, String name) {
        return resolve(context.getReferenceResolver(), name);
    }

    /**
     * Resolve a single-named configuration from a reference resolver.
     */
    public static NoSqlDatabaseConfiguration resolve(ReferenceResolver referenceResolver, String name) {
        if (name == null || name.isBlank()) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "NoSQL action requires a database configuration name");
        }

        if (referenceResolver == null) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    String.format("Unable to resolve NoSQL database configuration '%s': no reference resolver available", name));
        }

        if (referenceResolver.isResolvable(name)) {
            try {
                return referenceResolver.resolve(name, NoSqlDatabaseConfiguration.class);
            } catch (RuntimeException e) {
                throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                        String.format("Reference '%s' is not a NoSQL database configuration: %s", name, e.getMessage()), e);
            }
        }

        if (referenceResolver.isResolvable(REFERENCE_NAME)) {
            NoSqlDatabaseConfigurations configurations;
            try {
                configurations = referenceResolver.resolve(REFERENCE_NAME, NoSqlDatabaseConfigurations.class);
            } catch (RuntimeException e) {
                throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                        String.format("Reference '%s' is not a NoSQL database configuration collection: %s",
                                REFERENCE_NAME, e.getMessage()), e);
            }

            return configurations.require(name);
        }

        throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                String.format("Unknown NoSQL database configuration '%s'", name));
    }
}
