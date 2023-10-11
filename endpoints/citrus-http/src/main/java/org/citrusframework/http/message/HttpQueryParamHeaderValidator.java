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

package org.citrusframework.http.message;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.DefaultHeaderValidator;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class HttpQueryParamHeaderValidator extends DefaultHeaderValidator {

    @Override
    public void validateHeader(String name, Object received, Object control, TestContext context, HeaderValidationContext validationContext) {
        if (ValidationMatcherUtils.isValidationMatcherExpression(Optional.ofNullable(control)
                .map(Object::toString)
                .orElse(""))) {
            super.validateHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS, received, control, context, validationContext);
            return;
        }

        Map<String, String> receiveParams = convertToMap(received);
        Map<String, String> controlParams = convertToMap(control);

        for (Map.Entry<String, String> param : controlParams.entrySet()) {
            if (!receiveParams.containsKey(param.getKey())) {
                throw new ValidationException("Validation failed: Query param '" + param.getKey() + "' is missing");
            }

            super.validateHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS + "(" + param.getKey() + ")", receiveParams.get(param.getKey()), param.getValue(), context, validationContext);
        }
    }

    /**
     * Convert query string key-value expression to map.
     * @param expression
     * @return
     */
    private Map<String, String> convertToMap(Object expression) {
        if (expression instanceof Map) {
            return (Map<String, String>) expression;
        }

        return Stream.of(Optional.ofNullable(expression)
                .map(Object::toString)
                .orElse("")
                .split(","))
                .map(keyValue -> keyValue.split("="))
                .filter(keyValue -> StringUtils.hasText(keyValue[0]))
                .map(keyValue -> {
                    if (keyValue.length < 2) {
                        return new String[]{keyValue[0], ""};
                    }
                    return keyValue;
                })
                .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1]));
    }

    @Override
    public boolean supports(String headerName, Class<?> type) {
        return headerName.equals(HttpMessageHeaders.HTTP_QUERY_PARAMS);
    }
}
