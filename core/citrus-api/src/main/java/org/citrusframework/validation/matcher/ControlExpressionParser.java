/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.validation.matcher;

import java.util.List;

/**
 * Control expression parser for extracting the individual control values from a control expression.
 *
 * @author Martin Maher
 * @since 2.5
 */
public interface ControlExpressionParser {

    Character DEFAULT_DELIMITER = '\'';

    /**
     * Some validation matchers can optionally contain one or more control values nested within a control expression.
     * Matchers implementing this interface should take care of extracting the individual control values from the
     * expression. <br />
     * For example, the {@link org.citrusframework.validation.matcher.core.DateRangeValidationMatcher} expects
     * between 2 to 3 control values (dateFrom, dateTo and optionally datePattern) to be provided. It's
     * ControlExpressionParser would be expected to parse the control expression, returning each individual control
     * value for each nested parameter found within the control expression.
     *
     * @param controlExpression the control expression to be parsed
     * @param delimiter the delimiter to use. When {@literal NULL} the {@link #DEFAULT_DELIMITER} is assumed.
     * @return
     */
    List<String> extractControlValues(String controlExpression, Character delimiter);
}
