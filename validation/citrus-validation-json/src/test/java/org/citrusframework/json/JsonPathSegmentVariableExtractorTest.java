package org.citrusframework.json;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.variable.VariableExpressionSegmentMatcher;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JsonPathSegmentVariableExtractorTest extends UnitTestSupport {

    public static final String JSON_FIXTURE = "{\"name\": \"Peter\"}";

    private final JsonPathSegmentVariableExtractor unitUnderTest = new JsonPathSegmentVariableExtractor();

    @Test
    public void testExtractFromJson() {

        String jsonPath = "$.name";
        VariableExpressionSegmentMatcher matcher = matchSegmentExpressionMatcher(jsonPath);

        Assert.assertTrue(unitUnderTest.canExtract(context, JSON_FIXTURE, matcher));
        Assert.assertEquals(unitUnderTest.extractValue(context, JSON_FIXTURE, matcher), "Peter");
    }

    @Test
    public void testExtractFromNonJsonPathExpression() {
        String json = "{\"name\": \"Peter\"}";

        String nonJsonPath = "name";
        VariableExpressionSegmentMatcher matcher = matchSegmentExpressionMatcher(nonJsonPath);

        Assert.assertFalse(unitUnderTest.canExtract(context, json, matcher));
    }

    @Test
    public void testExtractFromJsonExpressionFailure() {
        String json = "{\"name\": \"Peter\"}";

        String invalidJsonPath = "$.$$$name";
        VariableExpressionSegmentMatcher matcher = matchSegmentExpressionMatcher(invalidJsonPath);

        Assert.assertTrue(unitUnderTest.canExtract(context, json, matcher));
        Assert.assertThrows(() -> unitUnderTest.extractValue(context, json, matcher));
    }

    /**
     * Create a variable expression jsonPath matcher and match the first jsonPath
     * @param jsonPath
     * @return
     */
    private VariableExpressionSegmentMatcher matchSegmentExpressionMatcher(String jsonPath) {
        String variableExpression = String.format("jsonPath(%s)", jsonPath);
        VariableExpressionSegmentMatcher matcher = new VariableExpressionSegmentMatcher(variableExpression);
        Assert.assertTrue(matcher.nextMatch());
        return matcher;
    }
}
