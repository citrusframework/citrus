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

package org.citrusframework.validation.context;

import java.util.Map;
import java.util.Optional;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic validation context holding validation specific information.
 *
 */
public interface ValidationContext {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(ValidationContext.class);

    /** Endpoint builder resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/validation/builder";

    /** Default Citrus validation context builders from classpath resource properties */
    ResourcePathTypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Indicates whether this validation context requires a validator.
     * @return true if a validator is required; false otherwise.
     */
    default boolean requiresValidator() {
        return false;
    }

    /**
     * Update the validation status if it is allowed.
     * @param status the new status.
     */
    void updateStatus(ValidationStatus status);

    /**
     * Marks the validation result for this context.
     * By default, all validation context do have the status UNKNOWN marking that the validation has not performed yet.
     * Validators must set proper status after the validation to mark the context as being processed.
     * @return the status indicating the validation result for this context.
     */
    default ValidationStatus getStatus() {
        return ValidationStatus.UNKNOWN;
    }

    /**
     * Resolves all available validation context builders from resource path lookup.
     * Scans classpath for validation context builder meta information and instantiates those builders.
     */
    static Map<String, Builder<?, ?>> lookup() {
        Map<String, Builder<?, ?>> builders = TYPE_RESOLVER.resolveAll();

        if (logger.isDebugEnabled()) {
            builders.forEach((k, v) -> logger.debug("Found validation context builder '{}' as {}", k, v.getClass()));
        }
        return builders;
    }

    /**
     * Resolves validation context builder from resource path lookup with given resource name.
     * Scans classpath for validation context builder meta information with given name and returns instance of the builder.
     * Returns optional instead of throwing exception when no validation context builder could be found.
     * <p>
     * Given builder name is a combination of resource file name and type property separated by '.' character.
     */
    static Optional<Builder<?, ?>> lookup(String builder) {
        try {
            return Optional.of(TYPE_RESOLVER.resolve(builder));
        } catch (CitrusRuntimeException e) {
            logger.warn("Failed to resolve validation context builder from resource '{}/{}'", RESOURCE_PATH, builder);
        }

        return Optional.empty();
    }

    /**
     * Fluent builder
     * @param <T> context type
     * @param <B> builder reference to self
     */
    @FunctionalInterface
    interface Builder<T extends ValidationContext, B extends Builder<T, B>> {

        /**
         * Builds new validation context instance.
         * @return the built context.
         */
        T build();
    }
}
