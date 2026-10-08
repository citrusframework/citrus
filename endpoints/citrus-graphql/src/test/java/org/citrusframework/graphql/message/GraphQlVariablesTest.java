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
package org.citrusframework.graphql.message;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GraphQlVariablesTest extends AbstractTestNGUnitTest {

    @Test
    public void shouldKeepJsonTypesOfSingleVariables() {
        context.setVariable("id", "42");
        Map<String, Object> single = new LinkedHashMap<>();
        single.put("first", 10);
        single.put("draft", true);
        single.put("id", "${id}");
        single.put("ratio", 0.5);

        assertThat(GraphQlVariables.resolve(single, null, context))
                .containsExactly(Map.entry("first", 10), Map.entry("draft", true), Map.entry("id", "42"), Map.entry("ratio", 0.5));
    }

    @Test
    public void shouldKeepStringFromCitrusVariableAsString() {
        context.setVariable("n", "10");

        assertThat(GraphQlVariables.resolve(Map.of("first", "${n}"), null, context))
                .containsExactly(Map.entry("first", "10"));
    }

    @Test
    public void shouldTypeValuesOfVariablesJsonAfterResolution() {
        context.setVariable("n", "10");

        assertThat(GraphQlVariables.resolve(Map.of(), "{\"first\": ${n}, \"title\": \"citrus:upperCase('dune')\"}", context))
                .containsExactly(Map.entry("first", 10), Map.entry("title", "DUNE"));
    }

    @Test
    public void shouldLetSingleVariablesOverrideVariablesJson() {
        assertThat(GraphQlVariables.resolve(Map.of("first", 5), "{\"first\": 10, \"after\": \"x\"}", context))
                .containsExactly(Map.entry("first", 5), Map.entry("after", "x"));
    }

    @Test
    public void shouldResolveStringsInsideNestedValues() {
        context.setVariable("tag", "sci-fi");

        assertThat(GraphQlVariables.resolve(Map.of("filter", Map.of("tags", List.of("${tag}", 3))), null, context))
                .containsExactly(Map.entry("filter", Map.of("tags", List.of("sci-fi", 3))));
    }

    @Test
    public void shouldReturnEmptyVariablesWhenNoneGiven() {
        assertThat(GraphQlVariables.resolve(Map.of(), "  ", context)).isEmpty();
        assertThat(GraphQlVariables.resolve(null, null, context)).isEmpty();
    }

    @Test
    public void shouldRejectInvalidVariablesJson() {
        assertThatThrownBy(() -> GraphQlVariables.resolve(Map.of(), "{\"first\": }", context))
                .isInstanceOf(ValidationException.class)
                .hasMessageStartingWith("Invalid GraphQL variables: ");
    }

    @Test
    public void shouldRejectVariablesJsonThatIsNoObject() {
        assertThatThrownBy(() -> GraphQlVariables.resolve(Map.of(), "[1, 2]", context))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Invalid GraphQL variables: must be a JSON object");
    }
}
