package org.citrusframework.citrus.json;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.util.IsJsonPredicate;
import org.citrusframework.citrus.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.citrus.variable.SegmentVariableExtractorRegistry;
import org.citrusframework.citrus.variable.VariableExpressionSegmentMatcher;
import com.jayway.jsonpath.InvalidPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
