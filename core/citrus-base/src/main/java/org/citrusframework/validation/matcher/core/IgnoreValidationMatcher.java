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

package org.citrusframework.validation.matcher.core;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.matcher.ValidationMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class IgnoreValidationMatcher implements ValidationMatcher {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(IgnoreValidationMatcher.class);

    @Override
    public void validate(String fieldName, String value, List<String> controlParameters, TestContext context) throws ValidationException {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Ignoring value for field '%s'", fieldName));
        }
    }
}
