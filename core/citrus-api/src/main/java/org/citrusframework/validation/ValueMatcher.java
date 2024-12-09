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

package org.citrusframework.validation;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public interface ValueMatcher {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(ValueMatcher.class);

    /** Message validator resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/value/matcher";

    /** Type resolver to find custom message validators on classpath via resource path lookup */
    TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    Map<String, ValueMatcher> validators = new ConcurrentHashMap<>();

    /**
     * Resolves all available validators from resource path lookup. Scans classpath for validator meta information
     * and instantiates those validators.
     * @return
     */
    static Map<String, ValueMatcher> lookup() {
        if (validators.isEmpty()) {
            validators.putAll(TYPE_RESOLVER.resolveAll());

            if (logger.isDebugEnabled()) {
                validators.forEach((k, v) -> logger.debug("Found validator '{}' as {}", k, v.getClass()));
            }
        }
        return validators;
    }

    /**
     * Resolves validator from resource path lookup with given validator resource name. Scans classpath for validator meta information
     * with given name and returns instance of validator. Returns optional instead of throwing exception when no validator
     * could be found.
     * @param validator
     * @return
     */
    static Optional<ValueMatcher> lookup(String validator) {
        try {
            ValueMatcher instance = TYPE_RESOLVER.resolve(validator);
            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            logger.warn("Failed to resolve value matcher from resource '{}/{}'", RESOURCE_PATH, validator);
        }

        return Optional.empty();
    }

    /**
     * Filter supported value types
     * @param controlType
     * @return
     */
    boolean supports(Class<?> controlType);

    /**
     * Value matcher verifies the match of given received and control values.
     * @param received
     * @param control
     * @param context
     */
    boolean validate(Object received, Object control, TestContext context);
}
