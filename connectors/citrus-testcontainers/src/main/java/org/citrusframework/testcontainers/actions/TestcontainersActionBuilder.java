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

package org.citrusframework.testcontainers.actions;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.testcontainers.aws2.StartLocalStackAction;
import org.citrusframework.testcontainers.kafka.StartKafkaAction;
import org.citrusframework.testcontainers.mongodb.StartMongoDBAction;
import org.citrusframework.testcontainers.postgresql.StartPostgreSQLAction;
import org.citrusframework.testcontainers.redpanda.StartRedpandaAction;
import org.citrusframework.util.ObjectHelper;
import org.testcontainers.containers.GenericContainer;

public class TestcontainersActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestcontainersAction> {

    private AbstractTestcontainersAction.Builder<? extends TestcontainersAction, ?> delegate;

    /**
     * Fluent API action building entry method used in Java DSL.
     * @return
     */
    public static TestcontainersActionBuilder testcontainers() {
        return new TestcontainersActionBuilder();
    }

    /**
     * Manage generic testcontainers.
     * @return
     */
    public GenericContainerActionBuilder container() {
        return new GenericContainerActionBuilder();
    }

    /**
     * Manage LocalStack testcontainers.
     * @return
     */
    public LocalStackActionBuilder localstack() {
        return new LocalStackActionBuilder();
    }

    /**
     * Manage PostgreSQL testcontainers.
     * @return
     */
    public PostgreSQLActionBuilder postgreSQL() {
        return new PostgreSQLActionBuilder();
    }

    /**
     * Manage MongoDB testcontainers.
     * @return
     */
    public MongoDBActionBuilder mongoDB() {
        return new MongoDBActionBuilder();
    }

    /**
     * Manage Redpanda testcontainers.
     * @return
     */
    public RedpandaActionBuilder redpanda() {
        return new RedpandaActionBuilder();
    }

    /**
     * Manage Redpanda testcontainers.
     * @return
     */
    public KafkaActionBuilder kafka() {
        return new KafkaActionBuilder();
    }

    @Override
    public TestcontainersAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
        return delegate.build();
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }

    public StartTestcontainersAction.Builder<GenericContainer<?>> start() {
        StartTestcontainersAction.Builder<GenericContainer<?>> builder = new StartTestcontainersAction.Builder<>();
        delegate = builder;
        return builder;
    }

    public StopTestcontainersAction.Builder stop() {
        StopTestcontainersAction.Builder builder = new StopTestcontainersAction.Builder();
        delegate = builder;
        return builder;
    }

    public class GenericContainerActionBuilder {
        /**
         * Start generic testcontainers instance.
         */
        public StartTestcontainersAction.Builder<?> start() {
            StartTestcontainersAction.Builder<?> builder = new StartTestcontainersAction.Builder<>();
            delegate = builder;
            return builder;
        }

        /**
         * Stop generic testcontainers instance.
         */
        public StopTestcontainersAction.Builder stop() {
            StopTestcontainersAction.Builder builder = new StopTestcontainersAction.Builder();
            delegate = builder;
            return builder;
        }
    }

    public class LocalStackActionBuilder {
        /**
         * Start LocalStack testcontainers instance.
         */
        public StartLocalStackAction.Builder start() {
            StartLocalStackAction.Builder builder = new StartLocalStackAction.Builder();
            delegate = builder;
            return builder;
        }

        /**
         * Stop LocalStack testcontainers instance.
         */
        public StopTestcontainersAction.Builder stop() {
            StopTestcontainersAction.Builder builder = new StopTestcontainersAction.Builder();
            delegate = builder;
            return builder;
        }
    }

    public class PostgreSQLActionBuilder {
        /**
         * Start PostgreSQL testcontainers instance.
         */
        public StartPostgreSQLAction.Builder start() {
            StartPostgreSQLAction.Builder builder = new StartPostgreSQLAction.Builder();
            delegate = builder;
            return builder;
        }

        /**
         * Stop PostgreSQL testcontainers instance.
         */
        public StopTestcontainersAction.Builder stop() {
            StopTestcontainersAction.Builder builder = new StopTestcontainersAction.Builder();
            delegate = builder;
            return builder;
        }
    }

    public class MongoDBActionBuilder {
        /**
         * Start MongoDB testcontainers instance.
         */
        public StartMongoDBAction.Builder start() {
            StartMongoDBAction.Builder builder = new StartMongoDBAction.Builder();
            delegate = builder;
            return builder;
        }

        /**
         * Stop MongoDB testcontainers instance.
         */
        public StopTestcontainersAction.Builder stop() {
            StopTestcontainersAction.Builder builder = new StopTestcontainersAction.Builder();
            delegate = builder;
            return builder;
        }
    }

    public class RedpandaActionBuilder {
        /**
         * Start Redpanda testcontainers instance.
         */
        public StartRedpandaAction.Builder start() {
            StartRedpandaAction.Builder builder = new StartRedpandaAction.Builder();
            delegate = builder;
            return builder;
        }

        /**
         * Stop Redpanda testcontainers instance.
         */
        public StopTestcontainersAction.Builder stop() {
            StopTestcontainersAction.Builder builder = new StopTestcontainersAction.Builder();
            delegate = builder;
            return builder;
        }
    }

    public class KafkaActionBuilder {
        /**
         * Start Kafka testcontainers instance.
         */
        public StartKafkaAction.Builder start() {
            StartKafkaAction.Builder builder = new StartKafkaAction.Builder();
            delegate = builder;
            return builder;
        }

        /**
         * Stop Kafka testcontainers instance.
         */
        public StopTestcontainersAction.Builder stop() {
            StopTestcontainersAction.Builder builder = new StopTestcontainersAction.Builder();
            delegate = builder;
            return builder;
        }
    }

}
