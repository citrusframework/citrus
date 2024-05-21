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

package org.citrusframework.variable;

import org.citrusframework.context.TestContext;

/**
 * Class extracting values of segments of VariableExpressions.
 */
public interface SegmentVariableExtractor {

    /**
     * Extract variables from given object.
     * @param object the object of which to extract the value
     * @param matcher
     */
    boolean canExtract(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher);

    /**
     * Extract variables from given object. Implementations should throw a CitrusRuntimeException
     * @param object the object of which to extract the value
     * @param matcher
     */
    Object extractValue(TestContext testContext, Object object, VariableExpressionSegmentMatcher matcher);

}
