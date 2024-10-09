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

package org.citrusframework.validation.matcher.hamcrest.provider;

import org.citrusframework.validation.matcher.hamcrest.HamcrestMatcherProvider;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.springframework.util.AntPathMatcher;

import static java.lang.String.format;

public class AntPathMatcherProvider implements HamcrestMatcherProvider {

    @Override
    public String getName() {
        return "matchesPath";
    }

    @Override
    public Matcher<String> provideMatcher(String predicate) {
        return new CustomMatcher<>(format("path matching %s", predicate)) {
            @Override
            public boolean matches(Object item) {
                return ((item instanceof String) && new AntPathMatcher().match(predicate, (String) item));
            }
        };
    }
}
