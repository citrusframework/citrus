/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.jdbc.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.rmi.registry.Registry;

import com.consol.citrus.annotations.CitrusEndpointConfig;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@CitrusEndpointConfig(qualifier = "jdbc.server")
public @interface JdbcServerConfig {

    /**
     * Auto start.
     * @return
     */
    boolean autoStart() default false;

    /**
     * Host.
     * @return
     */
    String host() default "";

    /**
     * Server port.
     * @return
     */
    int port() default Registry.REGISTRY_PORT;

    /**
     * Database name.
     * @return
     */
    String databaseName() default "";

    /**
     * Auto accept connections.
     * @return
     */
    boolean autoConnect() default true;

    /**
     * Auto create statements.
     * @return
     */
    boolean autoCreateStatement() default true;

    /**
     * Auto reply check connetion queries.
     * @return
     */
    String[] autoHandleQueries() default {
            "SELECT \\w*", //H2, MySQL, PostgreSQL, SQLite, Microsoft SQL Server
            "SELECT.*FROM DUAL", // Oracle
            "SELECT.*FROM SYSIBM.SYSDUMMY1" // DB2
    };

    /**
     * Message correlator.
     * @return
     */
    String correlator() default "";

    /**
     * Endpoint adapter reference.
     * @return
     */
    String endpointAdapter() default "";

    /**
     * Debug logging enabled.
     * @return
     */
    boolean debugLogging() default false;

    /**
     * Maximum number of connections.
     * @return
     */
    int maxConnections() default 20;

    /**
     * Polling interval.
     * @return
     */
    int pollingInterval() default 500;

    /**
     * Timeout.
     * @return
     */
    long timeout() default 5000L;

    /**
     * Test actor.
     * @return
     */
    String actor() default "";

    /**
     * Auto transaction handling
     * @return Whether autoTransactionHandling is enabled
     */
    boolean autoTransactionHandling() default true;
}
