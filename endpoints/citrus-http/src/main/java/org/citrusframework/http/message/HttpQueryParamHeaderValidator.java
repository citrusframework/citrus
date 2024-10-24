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

package org.citrusframework.http.message;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.DefaultHeaderValidator;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.citrusframework.http.message.HttpMessageHeaders.HTTP_QUERY_PARAMS;
import static org.citrusframework.util.StringUtils.hasText;

/**
 * @since 2.7.6
 */
public class HttpQueryParamHeaderValidator extends DefaultHeaderValidator {

    @Override
    public void validateHeader(String name, Object received, Object control, TestContext context, HeaderValidationContext validationContext) {
        if (ValidationMatcherUtils.isValidationMatcherExpression(Optional.ofNullable(control)
                .map(Object::toString)
                .orElse(""))) {
            super.validateHeader(HTTP_QUERY_PARAMS, received, control, context, validationContext);
            return;
        }

        // TODO Christoph Deppisch: I changed this to support multi lists, e.q. required for array query parameters.
        //  Not sure about consequences though. Therefore, i call a new method super.validateHeaderArray below.
        //  Maybe we should fix this in general.
        Map<String, Object> receiveParams = convertToMap(received);
        Map<String, Object> controlParams = convertToMap(control);

        for (Map.Entry<String, Object> param : controlParams.entrySet()) {
            if (!receiveParams.containsKey(param.getKey())) {
                throw new ValidationException("Validation failed: Query param '" + param.getKey() + "' is missing");
            }

            super.validateHeaderArray(HTTP_QUERY_PARAMS + "(" + param.getKey() + ")", receiveParams.get(param.getKey()), param.getValue(), context, validationContext);
        }
    }

    /**
     * Convert query string key-value expression to map. Note, that there could be hamcrest matchers
     * encoded in the expression.
     */
    private Map<String, Object> convertToMap(Object expression) {
        if (expression instanceof Map<?,?>) {
            return (Map<String, Object>) expression;
        }

        return Stream.of(Optional.ofNullable(expression)
                .map(Object::toString)
                .orElse("")
                .split(","))
            .map(keyValue -> keyValue.split("="))
            .filter(keyValue -> hasText(keyValue[0]))
            .collect(toMap(
                keyValue -> keyValue[0],  // Key function
                keyValue ->
                    // Value function: if no value is present, use an empty string
                     (keyValue.length < 2 ? "" : keyValue[1])
                ,
                (existingValue, newValue) -> {  // Merge function to handle duplicate keys
                    if (existingValue instanceof List<?>) {
                        ((List<String>) existingValue).add(newValue.toString());
                        return existingValue;
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add((String) existingValue);
                        list.add(newValue.toString());
                        return list;
                    }
                }
            ));
    }

    @Override
    public boolean supports(String headerName, Class<?> type) {
        return headerName.equals(HTTP_QUERY_PARAMS);
    }
}
