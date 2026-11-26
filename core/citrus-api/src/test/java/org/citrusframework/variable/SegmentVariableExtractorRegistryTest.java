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

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.variable.SegmentVariableExtractorRegistry.MapVariableExtractor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class SegmentVariableExtractorRegistryTest {

    @Test
    void shouldReportUnknownVariableForSingleSegment() {
        VariableExpressionSegmentMatcher matcher = new VariableExpressionSegmentMatcher("unknownVar");
        matcher.nextMatch();

        TestContext testContext = mock(TestContext.class);
        Map<String, Object> variables = new HashMap<>();

        CitrusRuntimeException exception = assertThrows(CitrusRuntimeException.class, () ->
                MapVariableExtractor.INSTANCE.extractValue(testContext, variables, matcher));
        assertEquals("Unknown variable 'unknownVar'", exception.getMessage());
    }

    @Test
    void shouldReportUnknownVariableForDottedFirstSegment() {
        VariableExpressionSegmentMatcher matcher = new VariableExpressionSegmentMatcher("http.server.port");
        matcher.nextMatch();

        TestContext testContext = mock(TestContext.class);
        Map<String, Object> variables = new HashMap<>();

        CitrusRuntimeException exception = assertThrows(CitrusRuntimeException.class, () ->
                MapVariableExtractor.INSTANCE.extractValue(testContext, variables, matcher));
        assertEquals("Unknown variable 'http.server.port'", exception.getMessage());
    }

    @Test
    void shouldReportUnknownSegmentValueForLaterSegment() {
        VariableExpressionSegmentMatcher matcher = new VariableExpressionSegmentMatcher("http.server.port");
        matcher.nextMatch();

        TestContext testContext = mock(TestContext.class);
        Map<String, Object> parentMap = new HashMap<>();
        parentMap.put("http", new HashMap<>());

        // First segment resolves, advance to second
        MapVariableExtractor.INSTANCE.extractValue(testContext, parentMap, matcher);
        matcher.nextMatch();

        CitrusRuntimeException exception = assertThrows(CitrusRuntimeException.class, () ->
                MapVariableExtractor.INSTANCE.extractValue(testContext, new HashMap<>(), matcher));
        assertEquals("Unknown segment-value for segment 'server' of variable expression 'http.server.port'",
                exception.getMessage());
    }
}
