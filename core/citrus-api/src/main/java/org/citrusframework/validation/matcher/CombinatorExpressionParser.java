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

package org.citrusframework.validation.matcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Splits the body of a combinator matcher expression (e.g. the content inside {@code allOf(...)})
 * into individual sub-matcher expressions. The split is comma-based but is both paren-depth-aware
 * and single-quote-aware so that commas inside nested parentheses or quoted strings are not treated
 * as separators.
 *
 * <p>Example: {@code startsWith('hello'), anyOf(endsWith('world'), contains('!'))} is split into
 * two elements: {@code ["startsWith('hello')", "anyOf(endsWith('world'), contains('!'))"]}.</p>
 */
public final class CombinatorExpressionParser {

    private CombinatorExpressionParser() {
        // utility class
    }

    /**
     * Splits {@code body} into sub-matcher expressions by commas at paren-depth 0 that are not
     * inside a single-quoted string. Leading and trailing whitespace is trimmed from each element.
     *
     * @param body the raw content between the outer parentheses of a combinator expression, e.g.
     *             {@code startsWith('a'), endsWith('b')}
     * @return ordered list of trimmed sub-expressions; empty list when {@code body} is null or blank
     */
    public static List<String> splitSubExpressions(String body) {
        if (body == null || body.isBlank()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        int depth = 0;
        boolean inQuote = false;
        int start = 0;

        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);

            if (c == '\'' && depth == 0) {
                inQuote = !inQuote;
            } else if (!inQuote) {
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                } else if (c == ',' && depth == 0) {
                    result.add(body.substring(start, i).trim());
                    start = i + 1;
                }
            }
        }

        // add the last (or only) segment
        String last = body.substring(start).trim();
        if (!last.isEmpty()) {
            result.add(last);
        }

        return result;
    }
}
