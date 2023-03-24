package org.citrusframework.validation;

import org.citrusframework.context.TestContext;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;

/**
 * @author Christoph Deppisch
 */
public class HamcrestValueMatcher implements ValueMatcher {

    @Override
    public boolean validate(Object received, Object control, TestContext context) {
        if (control instanceof Matcher) {
            return ((Matcher<?>) control).matches(received);
        } else {
            IsEqual<Object> equalMatcher = new IsEqual<>(control);
            return equalMatcher.matches(received);
        }
    }

    @Override
    public boolean supports(Class<?> controlType) {
        return Matcher.class.isAssignableFrom(controlType);
    }
}
