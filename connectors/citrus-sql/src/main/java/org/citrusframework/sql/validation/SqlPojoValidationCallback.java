/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.sql.validation;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.script.sql.SqlResultSetValidator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;

/**
 * Validates SQL result-set rows as typed POJOs instead of raw column maps.
 *
 * Each raw row is mapped to the caller type with case-insensitive, underscore-folded
 * column-to-property matching (consistent with control result-set validation), then
 * checked with {@link #validate(Object, TestContext)}. With several rows the callback
 * holds when ANY row satisfies the assertion — the common "the table contains a row
 * like this" question. Override {@link #validate(List, TestContext)} to take over
 * multi-row semantics; the empty list still reaches the override.
 *
 * @param <T> the POJO type rows are mapped to, needs a no-argument constructor and
 * JavaBean setters for the asserted properties.
 */
public abstract class SqlPojoValidationCallback<T> implements SqlResultSetValidator {

    private final Class<T> type;

    protected SqlPojoValidationCallback(Class<T> type) {
        this.type = type;
    }

    @Override
    public final void validateSqlResultSet(List<Map<String, Object>> resultSet, TestContext context) {
        List<T> rows = new ArrayList<>(resultSet.size());
        int index = 0;
        for (Map<String, Object> row : resultSet) {
            rows.add(mapRow(row, index, resultSet));
            index++;
        }
        validate(rows, context);
    }

    /**
     * Validates the mapped rows, ANY match wins. Fails naming the rows seen when none
     * satisfies the per-row assertion.
     */
    public void validate(List<T> rows, TestContext context) {
        if (rows.isEmpty()) {
            throw new ValidationException("SQL POJO validation failed for " + type.getSimpleName()
                    + ": query returned 0 rows, nothing to validate");
        }

        List<String> seen = new ArrayList<>(rows.size());
        Throwable lastFailure = null;
        for (T row : rows) {
            try {
                validate(row, context);
                return;
            } catch (ValidationException | AssertionError e) {
                lastFailure = e;
                seen.add(String.valueOf(row));
            }
        }

        throw new ValidationException("SQL POJO validation failed for " + type.getSimpleName()
                + ": none of " + rows.size() + " row(s) satisfied the assertion; rows seen: " + seen,
                lastFailure);
    }

    /**
     * Validates a single mapped row. Throw {@link ValidationException} (or use an
     * assertion library throwing {@link AssertionError}) to reject the row.
     */
    protected abstract void validate(T row, TestContext context);

    private T mapRow(Map<String, Object> row, int index, List<Map<String, Object>> resultSet) {
        T instance = BeanUtils.instantiateClass(type);
        BeanWrapper beanWrapper = new BeanWrapperImpl(instance);
        for (Map.Entry<String, Object> column : row.entrySet()) {
            String property = matchProperty(beanWrapper, column.getKey(), row, index);
            try {
                beanWrapper.setPropertyValue(property, column.getValue());
            } catch (BeansException e) {
                throw new ValidationException("Cannot convert column '" + column.getKey()
                        + "' (row " + index + ") to property '" + property + "' of "
                        + type.getSimpleName() + ": " + e.getMessage(), e);
            }
        }
        return instance;
    }

    private String matchProperty(BeanWrapper beanWrapper, String column,
            Map<String, Object> row, int index) {
        if (beanWrapper.isWritableProperty(column)) {
            return column;
        }

        String folded = column.replace("_", "");
        for (PropertyDescriptor descriptor : beanWrapper.getPropertyDescriptors()) {
            if (descriptor.getWriteMethod() != null && descriptor.getName().equalsIgnoreCase(folded)) {
                return descriptor.getName();
            }
        }

        throw new ValidationException("Cannot map column '" + column + "' (row " + index
                + ") to a property of " + type.getSimpleName()
                + "; available columns: " + row.keySet());
    }
}
