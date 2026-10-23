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
import java.util.OptionalInt;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.api.actions.nosql.NoSqlActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.nosql.config.NoSqlDatabaseConfiguration;
import org.citrusframework.nosql.config.NoSqlDatabaseConfigurations;
import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.nosql.model.NoSqlResult;
import org.citrusframework.nosql.model.NoSqlValue;
import org.citrusframework.nosql.spi.NoSqlOperation;
import org.citrusframework.nosql.spi.NoSqlOperationKind;
import org.citrusframework.nosql.spi.NoSqlOperationOptions;
import org.citrusframework.nosql.spi.NoSqlPayload;
import org.citrusframework.nosql.spi.NoSqlProvider;
import org.citrusframework.nosql.spi.NoSqlProviderContext;
import org.citrusframework.nosql.spi.NoSqlProviderRegistry;
import org.citrusframework.nosql.validation.NoSqlResultPaths;
import org.citrusframework.spi.ReferenceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic NoSQL test action. Resolves a named database configuration, checks provider capabilities
 * before any database call, executes the operation, normalizes the result and finally runs
 * validations and extractions.
 *
 * <p>The action is safe for parallel execution: the specification is immutable and no execution
 * state is stored on the action. Writes are never retried implicitly.</p>
 */
public class NoSqlAction extends AbstractTestAction {

    private static final Logger logger = LoggerFactory.getLogger(NoSqlAction.class);

    private final NoSqlActionSpec spec;
    private final ReferenceResolver referenceResolver;
    private volatile NoSqlProviderRegistry providers;

    public NoSqlAction(Builder builder) {
        super("nosql", builder);
        this.spec = builder.toSpec();
        this.providers = builder.providers;
        this.referenceResolver = builder.referenceResolver;
    }

    @Override
    public void doExecute(TestContext context) {
        NoSqlActionSpec effective = spec.substitute(context);

        ReferenceResolver resolver = context.getReferenceResolver() != null
                ? context.getReferenceResolver()
                : referenceResolver;
        NoSqlProvider provider;
        NoSqlOperation operation;
        NoSqlProviderContext providerContext;
        NoSqlDatabaseConfiguration configuration = NoSqlDatabaseConfigurations.resolve(resolver, effective.client());
        provider = providerRegistry().resolve(configuration.providerId());
        providerRegistry().checkSupport(provider, effective.kind(), effective.nativeCommand());
        requirePayload(effective);

        operation = new NoSqlOperation(effective.kind(), effective.nativeCommand(),
                effective.target(), effective.payload(),
                resolveOptions(effective, configuration));
        providerContext = new NoSqlProviderContext(configuration.name(),
                configuration.database(), configuration.client());

        logger.info("Executing NoSQL {} operation on target '{}' with configuration '{}'",
                effective.kind(), effective.target(), effective.client());

        NoSqlResult result = execute(provider, operation, providerContext, effective);
        if (result == null) {
            throw new NoSqlException(NoSqlErrorClass.EXECUTION,
                    String.format("NoSQL provider '%s' returned no result for operation '%s'",
                            provider.id(), effective.kind()));
        }

        NoSqlResultPaths.validate(result, effective.validations(), context);
        NoSqlResultPaths.extract(result, effective.extractions(), context);
    }

    private NoSqlResult execute(NoSqlProvider provider,
                                NoSqlOperation operation,
                                NoSqlProviderContext providerContext,
                                NoSqlActionSpec effective) {
        NoSqlRetryPolicy retryPolicy = effective.kind() == NoSqlOperationKind.QUERY
                ? effective.retryPolicy()
                : NoSqlRetryPolicy.none();

        for (int attempt = 1; ; attempt++) {
            try {
                return provider.execute(operation, providerContext);
            } catch (NoSqlException e) {
                NoSqlException normalized = normalize(e, effective.kind());
                if (attempt >= retryPolicy.maxAttempts() || !isRetryable(normalized)) {
                    throw normalized;
                }

                logger.debug("Retrying NoSQL {} operation on target '{}', attempt {} of {}",
                        effective.kind(), effective.target(), attempt + 1, retryPolicy.maxAttempts());
                sleep(retryPolicy.interval());
            } catch (RuntimeException e) {
                throw new NoSqlException(NoSqlErrorClass.EXECUTION,
                        String.format("NoSQL provider '%s' failed to execute '%s' on target '%s'",
                                provider.id(), effective.kind(), effective.target()), e);
            }
        }
    }

    /**
     * A write timeout always reports an unknown outcome, even when the provider did not flag it.
     * Native operations are left to the provider because only the provider knows whether a native
     * command reads or writes.
     */
    private NoSqlException normalize(NoSqlException exception, NoSqlOperationKind kind) {
        if (isWrite(kind)
                && exception.getErrorClass() == NoSqlErrorClass.TIMEOUT
                && !exception.isWriteOutcomeUnknown()) {
            return new NoSqlException(NoSqlErrorClass.TIMEOUT,
                    exception.getMessage() + " (write outcome unknown)", exception.getCause(), true);
        }

        return exception;
    }

    private boolean isRetryable(NoSqlException exception) {
        return exception.getErrorClass() == NoSqlErrorClass.CONNECTION
                || exception.getErrorClass() == NoSqlErrorClass.TIMEOUT;
    }

    private void sleep(Duration interval) {
        if (interval.isZero()) {
            return;
        }

        try {
            Thread.sleep(interval.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NoSqlException(NoSqlErrorClass.EXECUTION, "Interrupted while retrying NoSQL read operation", e);
        }
    }

    private void requirePayload(NoSqlActionSpec effective) {
        if (effective.requiresPayload() && effective.payload().isEmpty()) {
            throw new NoSqlException(NoSqlErrorClass.INVALID_PAYLOAD,
                    String.format("NoSQL operation '%s' requires a payload", effective.kind()));
        }
    }

    /**
     * Resolve the operation options. The action value wins, otherwise the named configuration may
     * provide a default for the result size.
     */
    private NoSqlOperationOptions resolveOptions(NoSqlActionSpec effective, NoSqlDatabaseConfiguration configuration) {
        if (effective.maxResults().isPresent()) {
            return NoSqlOperationOptions.maxResults(effective.maxResults().getAsInt());
        }

        NoSqlValue defaultValue = configuration.defaults().get("maxResults");
        if (defaultValue instanceof NoSqlValue.NumberValue numberValue) {
            try {
                return NoSqlOperationOptions.maxResults(numberValue.value().intValueExact());
            } catch (ArithmeticException e) {
                throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                        String.format("Default max results of configuration '%s' must be an integer",
                                configuration.name()), e);
            }
        }

        return NoSqlOperationOptions.defaults();
    }

    private boolean isWrite(NoSqlOperationKind kind) {
        return kind == NoSqlOperationKind.INSERT
                || kind == NoSqlOperationKind.UPDATE
                || kind == NoSqlOperationKind.DELETE;
    }

    private NoSqlProviderRegistry providerRegistry() {
        NoSqlProviderRegistry current = providers;
        if (current == null) {
            synchronized (this) {
                current = providers;
                if (current == null) {
                    current = NoSqlProviderRegistry.lookup();
                    providers = current;
                }
            }
        }

        return current;
    }

    public NoSqlActionSpec getSpec() {
        return spec;
    }

    public static class Builder extends AbstractTestActionBuilder<NoSqlAction, Builder>
            implements NoSqlActionBuilder<NoSqlAction, Builder> {

        private String client;
        private NoSqlOperationKind kind;
        private String nativeCommand;
        private String target;
        private String payload;
        private String filter;
        private boolean payloadSet;
        private boolean filterSet;
        private final List<NoSqlValidation> validations = new ArrayList<>();
        private final List<NoSqlExtraction> extractions = new ArrayList<>();
        private int maxResults;
        private boolean maxResultsSet;
        private NoSqlRetryPolicy retryPolicy = NoSqlRetryPolicy.none();
        private NoSqlProviderRegistry providers;
        private ReferenceResolver referenceResolver;

        @Override
        public Builder withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return this;
        }

        @Override
        public Builder client(String client) {
            this.client = client;
            return this;
        }

        @Override
        public Builder query() {
            this.kind = NoSqlOperationKind.QUERY;
            this.nativeCommand = null;
            return this;
        }

        @Override
        public Builder insert() {
            this.kind = NoSqlOperationKind.INSERT;
            this.nativeCommand = null;
            return this;
        }

        @Override
        public Builder update() {
            this.kind = NoSqlOperationKind.UPDATE;
            this.nativeCommand = null;
            return this;
        }

        @Override
        public Builder delete() {
            this.kind = NoSqlOperationKind.DELETE;
            this.nativeCommand = null;
            return this;
        }

        @Override
        public Builder nativeOperation(String command) {
            this.kind = NoSqlOperationKind.NATIVE;
            this.nativeCommand = command;
            return this;
        }

        @Override
        public Builder target(String target) {
            this.target = target;
            return this;
        }

        @Override
        public Builder payload(String payload) {
            this.payload = payload;
            this.payloadSet = true;
            return this;
        }

        @Override
        public Builder filter(String filter) {
            this.filter = filter;
            this.filterSet = true;
            return this;
        }

        @Override
        public Builder validate(String path, String expected) {
            validations.add(new NoSqlValidation(path, expected));
            return this;
        }

        @Override
        public Builder extract(String path, String variable) {
            extractions.add(new NoSqlExtraction(path, variable));
            return this;
        }

        @Override
        public Builder maxResults(int maxResults) {
            this.maxResults = maxResults;
            this.maxResultsSet = true;
            return this;
        }

        @Override
        public Builder retry(int maxAttempts, Duration interval) {
            this.retryPolicy = NoSqlRetryPolicy.of(maxAttempts, interval);
            return this;
        }

        /**
         * Retry policy with a text interval, used by the XML and YAML adapters.
         */
        public Builder retry(int maxAttempts, String interval) {
            this.retryPolicy = NoSqlRetryPolicy.of(maxAttempts, interval);
            return this;
        }

        /**
         * Select the operation from its DSL name, used by the XML and YAML adapters.
         */
        public Builder operation(String operation, String nativeCommand) {
            switch (operation == null ? "" : operation) {
                case "query" -> query();
                case "insert" -> insert();
                case "update" -> update();
                case "delete" -> delete();
                case "native" -> nativeOperation(nativeCommand);
                default -> throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                        String.format("Unsupported NoSQL operation '%s'", operation));
            }

            return this;
        }

        /**
         * Use an explicit provider, mainly for tests and custom setups without classpath discovery.
         */
        public Builder provider(NoSqlProvider provider) {
            this.providers = NoSqlProviderRegistry.of(provider);
            return this;
        }

        /**
         * Use an explicit provider registry, mainly for tests and custom setups.
         */
        public Builder providers(NoSqlProviderRegistry providers) {
            this.providers = providers;
            return this;
        }

        @Override
        public NoSqlAction build() {
            return new NoSqlAction(this);
        }

        private NoSqlActionSpec toSpec() {
            if (kind == null) {
                throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                        "NoSQL action requires an operation: query, insert, update, delete or nativeOperation");
            }

            if (payloadSet && filterSet) {
                throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                        "NoSQL action accepts either a filter or a payload, but not both");
            }

            String payloadText = filterSet ? filter : payload;
            OptionalInt limit = maxResultsSet ? OptionalInt.of(maxResults) : OptionalInt.empty();
            return new NoSqlActionSpec(client, kind, nativeCommand, target,
                    NoSqlPayload.text(payloadText == null ? "" : payloadText),
                    validations, extractions, limit, retryPolicy);
        }
    }
}
