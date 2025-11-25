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

package org.citrusframework.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.SegmentEvaluationException;
import org.citrusframework.util.IsJsonPredicate;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.variable.SegmentVariableExtractorRegistry;
import org.citrusframework.variable.VariableExpressionSegmentMatcher;

public class JsonPathSegmentVariableExtractor extends
    SegmentVariableExtractorRegistry.AbstractSegmentVariableExtractor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean canExtract(TestContext testContext, Object object,
        VariableExpressionSegmentMatcher matcher) {
        return object == null || (object instanceof String string && IsJsonPredicate.getInstance()
            .test(string) && JsonPathMessageValidationContext.isJsonPathExpression(
            matcher.getSegmentExpression()));
    }

    @Override
    public Object doExtractValue(TestContext testContext, Object object,
        VariableExpressionSegmentMatcher matcher) throws SegmentEvaluationException {
        try {
            return object == null ? null
                : extractJsonPath(object.toString(), matcher.getSegmentExpression());
        } catch (Exception e) {
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append(e.getMessage());
            if (e.getCause() != e) {
                messageBuilder.append("/");
                messageBuilder.append(e.getCause().getMessage());
            }
            throw new SegmentEvaluationException(messageBuilder.toString(), renderObject(object));
        }
    }

    private static String renderObject(Object object) {
        if (object == null) {
            return "null";
        }
        try {
            if (object instanceof CharSequence cs) {
                String string = cs.toString();
                if (looksLikeJson(string)) {
                    return prettyJson(string);
                }
                return string;
            }
            if (object instanceof java.util.Map || object instanceof java.util.Collection) {
                return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            }
        } catch (Exception ignore) {
            /* fall back to toString */
        }

        return String.valueOf(object);
    }

    private static boolean looksLikeJson(String s) {
        String t = s.stripLeading();
        return t.startsWith("{") || t.startsWith("[");
    }

    private static String prettyJson(String json) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter()
                .writeValueAsString(MAPPER.readTree(json));
        } catch (Exception e) {
            return json;
        }
    }

    private Object extractJsonPath(String json, String segmentExpression) {
        return JsonPathUtils.evaluate(json, segmentExpression);
    }
}
