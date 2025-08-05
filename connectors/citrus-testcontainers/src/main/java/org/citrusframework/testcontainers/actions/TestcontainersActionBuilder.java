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
import org.citrusframework.actions.testcontainers.TestcontainersComposeActionBuilder;
import org.citrusframework.actions.testcontainers.TestcontainersGenericContainerActionBuilder;
import org.citrusframework.actions.testcontainers.TestcontainersKafkaActionBuilder;
import org.citrusframework.actions.testcontainers.TestcontainersLocalStackActionBuilder;
import org.citrusframework.actions.testcontainers.TestcontainersMongoDBActionBuilder;
import org.citrusframework.actions.testcontainers.TestcontainersPostgreSQLActionBuilder;
import org.citrusframework.actions.testcontainers.TestcontainersRedpandaActionBuilder;
import org.citrusframework.testcontainers.aws2.StartLocalStackAction;
import org.citrusframework.testcontainers.compose.ComposeDownAction;
import org.citrusframework.testcontainers.compose.ComposeUpAction;
import org.citrusframework.testcontainers.kafka.StartKafkaAction;
import org.citrusframework.testcontainers.mongodb.StartMongoDBAction;
import org.citrusframework.testcontainers.postgresql.StartPostgreSQLAction;
import org.citrusframework.testcontainers.redpanda.StartRedpandaAction;
import org.citrusframework.util.ObjectHelper;
import org.testcontainers.containers.GenericContainer;

public class TestcontainersActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestcontainersAction>,
        org.citrusframework.actions.testcontainers.TestcontainersActionBuilder<TestcontainersAction, TestcontainersActionBuilder> {

    private AbstractTestcontainersAction.Builder<? extends TestcontainersAction, ?> delegate;

    /**
     * Fluent API action building entry method used in Java DSL.
     * @return
     */
    public static TestcontainersActionBuilder testcontainers() {
        return new TestcontainersActionBuilder();
    }

    @Override
    public GenericContainerActionBuilder container() {
        return new GenericContainerActionBuilder();
    }

    @Override
    public LocalStackActionBuilder localstack() {
        return new LocalStackActionBuilder();
    }

    @Override
    public PostgreSQLActionBuilder postgreSQL() {
        return new PostgreSQLActionBuilder();
    }

    @Override
    public MongoDBActionBuilder mongoDB() {
        return new MongoDBActionBuilder();
    }

    @Override
    public RedpandaActionBuilder redpanda() {
        return new RedpandaActionBuilder();
    }

    @Override
    public KafkaActionBuilder kafka() {
        return new KafkaActionBuilder();
    }

    @Override
    public ComposeActionBuilder compose() {
        return new ComposeActionBuilder();
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

    @Override
    public StartTestcontainersAction.Builder<GenericContainer<?>> start() {
        StartTestcontainersAction.Builder<GenericContainer<?>> builder = new StartTestcontainersAction.Builder<>();
        delegate = builder;
        return builder;
    }

    @Override
    public StopTestcontainersAction.Builder stop() {
        StopTestcontainersAction.Builder builder = new StopTestcontainersAction.Builder();
        delegate = builder;
        return builder;
    }

    public class GenericContainerActionBuilder implements TestcontainersGenericContainerActionBuilder {

        @Override
        public StartTestcontainersAction.Builder<?> start() {
            StartTestcontainersAction.Builder<?> builder = new StartTestcontainersAction.Builder<>();
            delegate = builder;
            return builder;
        }

        @Override
        public StopTestcontainersAction.Builder stop() {
            StopTestcontainersAction.Builder builder = new StopTestcontainersAction.Builder();
            delegate = builder;
            return builder;
        }
    }

    public class ComposeActionBuilder implements TestcontainersComposeActionBuilder {

        @Override
        public ComposeUpAction.Builder up() {
            ComposeUpAction.Builder builder = new ComposeUpAction.Builder();
            delegate = builder;
            return builder;
        }

        @Override
        public ComposeDownAction.Builder down() {
            ComposeDownAction.Builder builder = new ComposeDownAction.Builder();
            delegate = builder;
            return builder;
        }
    }

    public class LocalStackActionBuilder implements TestcontainersLocalStackActionBuilder {

        @Override
        public StartLocalStackAction.Builder start() {
            StartLocalStackAction.Builder builder = new StartLocalStackAction.Builder();
            delegate = builder;
            return builder;
        }

        @Override
        public StopTestcontainersAction.Builder stop() {
            StopTestcontainersAction.Builder builder = new StopTestcontainersAction.Builder();
            delegate = builder;
            return builder;
        }
    }

    public class PostgreSQLActionBuilder implements TestcontainersPostgreSQLActionBuilder {

        @Override
        public StartPostgreSQLAction.Builder start() {
            StartPostgreSQLAction.Builder builder = new StartPostgreSQLAction.Builder();
            delegate = builder;
            return builder;
        }

        @Override
        public StopTestcontainersAction.Builder stop() {
            StopTestcontainersAction.Builder builder = new StopTestcontainersAction.Builder();
            delegate = builder;
            return builder;
        }
    }

    public class MongoDBActionBuilder implements TestcontainersMongoDBActionBuilder {

        @Override
        public StartMongoDBAction.Builder start() {
            StartMongoDBAction.Builder builder = new StartMongoDBAction.Builder();
            delegate = builder;
            return builder;
        }

        @Override
        public StopTestcontainersAction.Builder stop() {
            StopTestcontainersAction.Builder builder = new StopTestcontainersAction.Builder();
            delegate = builder;
            return builder;
        }
    }

    public class RedpandaActionBuilder implements TestcontainersRedpandaActionBuilder {

        @Override
        public StartRedpandaAction.Builder start() {
            StartRedpandaAction.Builder builder = new StartRedpandaAction.Builder();
            delegate = builder;
            return builder;
        }

        @Override
        public StopTestcontainersAction.Builder stop() {
            StopTestcontainersAction.Builder builder = new StopTestcontainersAction.Builder();
            delegate = builder;
            return builder;
        }
    }

    public class KafkaActionBuilder implements TestcontainersKafkaActionBuilder {

        @Override
        public StartKafkaAction.Builder start() {
            StartKafkaAction.Builder builder = new StartKafkaAction.Builder();
            delegate = builder;
            return builder;
        }

        @Override
        public StopTestcontainersAction.Builder stop() {
            StopTestcontainersAction.Builder builder = new StopTestcontainersAction.Builder();
            delegate = builder;
            return builder;
        }
    }

}
