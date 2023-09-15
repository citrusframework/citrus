/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.validation.matcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General validation matcher interface.
 *
 * @author Christian Wied
 */
@FunctionalInterface
public interface ValidationMatcher {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(ValidationMatcher.class);

    /** Message validator resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/validation/matcher";

    Map<String, ValidationMatcher> matcher = new HashMap<>();

    /**
     * Resolves all available validators from resource path lookup. Scans classpath for validator meta information
     * and instantiates those validators.
     * @return
     */
    static Map<String, ValidationMatcher> lookup() {
        if (matcher.isEmpty()) {
            matcher.putAll(new ResourcePathTypeResolver().resolveAll(RESOURCE_PATH));

            if (logger.isDebugEnabled()) {
                matcher.forEach((k, v) -> logger.debug(String.format("Found validation matcher '%s' as %s", k, v.getClass())));
            }
        }
        return matcher;
    }

    /**
     * Method called on validation.
     *
     * @param fieldName the fieldName for logging purpose.
     * @param value the value to be validated.
     * @param controlParameters the control parameters.
     * @param context
     * @throws ValidationException when validation fails
     */
    void validate(String fieldName, String value, List<String> controlParameters, TestContext context) throws ValidationException;
}
