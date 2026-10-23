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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.citrusframework.api.common.ShutdownPhase;
import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.nosql.model.NoSqlValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Named database configuration referenced from all DSLs. Owns the provider id, the provider
 * managed client, the logical database and optional default options.
 *
 * <p>The client is never rendered or logged. Credentials belong to the client configuration and
 * must not be part of this object or of a test action.</p>
 *
 * <p>When the configuration is bound to a Citrus reference resolver, the resolver destroys it with
 * the context. An {@link AutoCloseable} client is closed once, so a forgotten manual close does not
 * leak the client for the lifetime of the JVM.</p>
 */
public final class NoSqlDatabaseConfiguration implements ShutdownPhase, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(NoSqlDatabaseConfiguration.class);

    private final String name;
    private final String providerId;
    private final String database;
    private final Object client;
    private final Map<String, NoSqlValue> defaults;
    private final AtomicBoolean destroyed = new AtomicBoolean(false);

    private NoSqlDatabaseConfiguration(Builder builder) {
        this.name = builder.name;
        this.providerId = builder.providerId;
        this.database = builder.database;
        this.client = builder.client;
        this.defaults = Collections.unmodifiableMap(new LinkedHashMap<>(builder.defaults));
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public String name() {
        return name;
    }

    public String providerId() {
        return providerId;
    }

    public String database() {
        return database;
    }

    public Object client() {
        return client;
    }

    public Map<String, NoSqlValue> defaults() {
        return defaults;
    }

    /**
     * Closes the managed client once. Called by the Citrus reference resolver when the context shuts
     * down, so an unclosed manual binding does not leak the client.
     */
    @Override
    public void destroy() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }

        if (client instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                logger.warn("Failed to close NoSQL client for configuration '{}': {}", name, e.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Spring infers {@code close} as a bean destroy method, so the configuration closes its client
     * even when the application does not declare an explicit destroy method.
     */
    @Override
    public void close() {
        destroy();
    }

    @Override
    public String toString() {
        return String.format("NoSqlDatabaseConfiguration[name=%s, provider=%s, database=%s]",
                name, providerId, database);
    }

    public static final class Builder {

        private final String name;
        private String providerId;
        private String database;
        private Object client;
        private final Map<String, NoSqlValue> defaults = new LinkedHashMap<>();

        private Builder(String name) {
            if (name == null || name.isBlank()) {
                throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                        "NoSQL database configuration requires a non-blank name");
            }
            this.name = name;
        }

        public Builder provider(String providerId) {
            this.providerId = providerId;
            return this;
        }

        public Builder database(String database) {
            this.database = database;
            return this;
        }

        public Builder client(Object client) {
            this.client = client;
            return this;
        }

        public Builder defaultOption(String name, NoSqlValue value) {
            defaults.put(Objects.requireNonNull(name, "Default option name must not be null"),
                    Objects.requireNonNull(value, "Default option value must not be null"));
            return this;
        }

        public NoSqlDatabaseConfiguration build() {
            if (providerId == null || providerId.isBlank()) {
                throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                        String.format("NoSQL database configuration '%s' requires a provider id", name));
            }

            if (client == null) {
                throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                        String.format("NoSQL database configuration '%s' requires a client", name));
            }

            return new NoSqlDatabaseConfiguration(this);
        }
    }
}
