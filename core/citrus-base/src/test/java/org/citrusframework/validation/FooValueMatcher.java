package org.citrusframework.validation;

import org.citrusframework.context.TestContext;

/**
 * @author Christoph Deppisch
 */
public class FooValueMatcher implements ValueMatcher {
    @Override
    public boolean supports(Class<?> controlType) {
        return FooValue.class.isAssignableFrom(controlType);
    }

    @Override
    public boolean validate(Object received, Object control, TestContext context) {
        return ((FooValue) received).value.equals(((FooValue) control).value);
    }

    static class FooValue {
        public final String value;

        FooValue(String value) {
            this.value = value;
        }
    }
}
