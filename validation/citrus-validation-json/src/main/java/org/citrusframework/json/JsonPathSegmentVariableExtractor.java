package org.citrusframework.json;

import com.jayway.jsonpath.InvalidPathException;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.IsJsonPredicate;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.variable.SegmentVariableExtractorRegistry;
import org.citrusframework.variable.VariableExpressionSegmentMatcher;

/**
 * @author Thorsten Schlathoelter
 */
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
