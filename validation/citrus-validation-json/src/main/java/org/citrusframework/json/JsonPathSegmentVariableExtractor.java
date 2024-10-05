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

import com.jayway.jsonpath.InvalidPathException;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.IsJsonPredicate;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.variable.SegmentVariableExtractorRegistry;
import org.citrusframework.variable.VariableExpressionSegmentMatcher;

public class JsonPathSegmentVariableExtractor extends SegmentVariableExtractorRegistry.AbstractSegmentVariableExtractor {

    @Override
    public boolean canExtract(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher) {
        return object == null  || (object instanceof  String && IsJsonPredicate.getInstance().test((String)object) && JsonPathMessageValidationContext.isJsonPathExpression(matcher.getSegmentExpression()));
    }

    @Override
    public Object doExtractValue(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher) {
        return object == null ? null : extractJsonPath(object.toString(), matcher.getSegmentExpression());
    }

    private Object extractJsonPath(String json, String segmentExpression) {
        try {
            return JsonPathUtils.evaluate(json, segmentExpression);
        } catch (InvalidPathException e) {
            throw new CitrusRuntimeException(String.format("Unable to extract jsonPath from segmentExpression %s", segmentExpression), e);
        }
    }
}
