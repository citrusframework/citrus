/*
 * Copyright 2006-2018 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public interface HeaderValidator {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(HeaderValidator.class);

    /** Header validator resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/header/validator";

    /** Type resolver to find custom message validators on classpath via resource path lookup */
    TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    Map<String, HeaderValidator> validators = new HashMap<>();

    /**
     * Resolves all available validators from resource path lookup. Scans classpath for validator meta information
     * and instantiates those validators.
     * @return
     */
    static Map<String, HeaderValidator> lookup() {
        if (validators.isEmpty()) {
            validators.putAll(TYPE_RESOLVER.resolveAll("", TypeResolver.DEFAULT_TYPE_PROPERTY, "name"));

            if (logger.isDebugEnabled()) {
                validators.forEach((k, v) -> logger.debug(String.format("Found header validator '%s' as %s", k, v.getClass())));
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
    static Optional<HeaderValidator> lookup(String validator) {
        try {
            HeaderValidator instance = TYPE_RESOLVER.resolve(validator);
            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            logger.warn(String.format("Failed to resolve header validator from resource '%s/%s'", RESOURCE_PATH, validator));
        }

        return Optional.empty();
    }

    /**
     * Filter supported headers by name and value type
     * @param headerName
     * @param type
     * @return
     */
    boolean supports(String headerName, Class<?> type);

    /**
     * Validate header values with received value and control value.
     * @param name
     * @param received
     * @param control
     * @param context
     * @param validationContext
     */
    void validateHeader(String name, Object received, Object control, TestContext context, HeaderValidationContext validationContext);
}
