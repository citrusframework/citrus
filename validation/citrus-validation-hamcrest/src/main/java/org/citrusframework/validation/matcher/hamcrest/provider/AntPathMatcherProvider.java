package org.citrusframework.validation.matcher.hamcrest.provider;

import org.citrusframework.validation.matcher.hamcrest.HamcrestMatcherProvider;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.springframework.util.AntPathMatcher;

/**
 * @author Christoph Deppisch
 */
public class AntPathMatcherProvider implements HamcrestMatcherProvider {

    @Override
    public String getName() {
        return "matchesPath";
    }

    @Override
    public Matcher<String> provideMatcher(String predicate) {
        return new CustomMatcher<String>(String.format("path matching %s", predicate)) {
            @Override
            public boolean matches(Object item) {
                return ((item instanceof String) && new AntPathMatcher().match(predicate, (String) item));
            }
        };
    }
}
