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

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.variable.VariableExpressionSegmentMatcher;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

public class JsonPathSegmentVariableExtractorTest extends UnitTestSupport {

    public static final String JSON_FIXTURE = """
        {
            "name": "Peter",
            "married": true,
            "wife": {
                "name": "Linda",
                "married": true,
                "pets": null
            },
            "children": [
                {
                    "name": "Paul",
                    "married": true,
                    "pets": null
                },
                {
                    "name": "Laura",
                    "married": false,
                    "pets": null
                }
            ],
            "pets": null
        }""";

    private final JsonPathSegmentVariableExtractor unitUnderTest = new JsonPathSegmentVariableExtractor();

    @Test
    public void succeedToExtractExistingFromJson() {

        String jsonPath = "$.name";
        VariableExpressionSegmentMatcher matcher = matchSegmentExpressionMatcher(jsonPath);

        assertThat(unitUnderTest.canExtract(context, JSON_FIXTURE, matcher)).isTrue();
        assertThat(unitUnderTest.extractValue(context, JSON_FIXTURE, matcher)).isEqualTo("Peter");
    }

    @Test
    public void succeedToExtractExistingFromJsonArray() {

        String jsonPath = "$.children[1].name";
        VariableExpressionSegmentMatcher matcher = matchSegmentExpressionMatcher(jsonPath);

        assertThat(unitUnderTest.canExtract(context, JSON_FIXTURE, matcher)).isTrue();
        assertThat(unitUnderTest.extractValue(context, JSON_FIXTURE, matcher)).isEqualTo("Laura");
    }

    @Test
    public void succeedToExtractNullFromExistingJsonArrayElement() {

        String jsonPath = "$.children[1].pets";
        VariableExpressionSegmentMatcher matcher = matchSegmentExpressionMatcher(jsonPath);

        assertThat(unitUnderTest.canExtract(context, JSON_FIXTURE, matcher)).isTrue();
        assertThat(unitUnderTest.extractValue(context, JSON_FIXTURE, matcher)).isNull();
    }

    @Test
    public void failsToExtractNonExistingPath() {

        String jsonPath = "$.wife.sex";
        VariableExpressionSegmentMatcher matcher = matchSegmentExpressionMatcher(jsonPath);

        assertThatThrownBy(() -> unitUnderTest.extractValue(context, JSON_FIXTURE, matcher))
            .isInstanceOf(CitrusRuntimeException.class)
            .extracting(Throwable::getMessage, STRING)
            .isEqualToNormalizingNewlines("""
                Unable to extract value using expression 'jsonPath($.wife.sex)'
                Reason: Failed to evaluate JSON path expression: $.wife.sex/No results for path: $['wife']['sex']
                From object (java.lang.String):
                {
                  "name" : "Peter",
                  "married" : true,
                  "wife" : {
                    "name" : "Linda",
                    "married" : true,
                    "pets" : null
                  },
                  "children" : [ {
                    "name" : "Paul",
                    "married" : true,
                    "pets" : null
                  }, {
                    "name" : "Laura",
                    "married" : false,
                    "pets" : null
                  } ],
                  "pets" : null
                }""");

    }

    @Test
    public void testExtractNullFromJson() {

        String jsonPath = "$.pets";
        VariableExpressionSegmentMatcher matcher = matchSegmentExpressionMatcher(jsonPath);

        assertThat(unitUnderTest.canExtract(context, JSON_FIXTURE, matcher)).isTrue();
        assertThat(unitUnderTest.extractValue(context, JSON_FIXTURE, matcher)).isNull();
    }

    @Test
    public void testExtractFromNonJsonPathExpression() {
        String json = "{\"name\": \"Peter\"}";

        String nonJsonPath = "name";
        VariableExpressionSegmentMatcher matcher = matchSegmentExpressionMatcher(nonJsonPath);

        assertThat(unitUnderTest.canExtract(context, json, matcher)).isFalse();
    }

    @Test
    public void throwOnInvalidJsonPathExpression() {
        String json = "{\"name\": \"Peter\"}";

        String invalidJsonPath = "$.$$$name";
        VariableExpressionSegmentMatcher matcher = matchSegmentExpressionMatcher(invalidJsonPath);

        assertThat(unitUnderTest.canExtract(context, json, matcher)).isTrue();
        assertThatThrownBy(() -> unitUnderTest.extractValue(context, json, matcher)).isInstanceOf(
            CitrusRuntimeException.class);
    }

    /**
     * Create a variable expression jsonPath matcher and match the jsonPath
     */
    private VariableExpressionSegmentMatcher matchSegmentExpressionMatcher(String jsonPath) {
        String variableExpression = String.format("jsonPath(%s)", jsonPath);
        VariableExpressionSegmentMatcher matcher = new VariableExpressionSegmentMatcher(
            variableExpression);
        assertThat(matcher.nextMatch()).isTrue();
        return matcher;
    }
}
