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

package org.citrusframework.spring.config.xml;

import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.citrusframework.spring.validation.matcher.CustomValidationMatcher;
import org.citrusframework.validation.matcher.ValidationMatcherLibrary;
import org.citrusframework.base.validation.matcher.core.EndsWithValidationMatcher;
import org.citrusframework.base.validation.matcher.core.IsNumberValidationMatcher;
import org.citrusframework.base.validation.matcher.core.StartsWithValidationMatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * @since 2.0
 */
public class ValidationMatcherLibraryParserTest extends AbstractBeanDefinitionParserTest {

    @BeforeAll
    @Override
    protected void parseBeanDefinitions() {
    }

    @Test
    public void testValidationMatcherParser() {
        beanDefinitionContext = createApplicationContext("context");
        Map<String, ValidationMatcherLibrary> matcherLibraries = beanDefinitionContext.getBeansOfType(ValidationMatcherLibrary.class);

        Assertions.assertEquals(2L, matcherLibraries.size());

        ValidationMatcherLibrary matcherLibraryBean = matcherLibraries.get("matcherLib");
        Assertions.assertEquals("matcherLib", matcherLibraryBean.getName());
        Assertions.assertEquals("foo", matcherLibraryBean.getPrefix());
        Assertions.assertEquals(3L, matcherLibraryBean.getMembers().size());
        Assertions.assertEquals(StartsWithValidationMatcher.class, matcherLibraryBean.getMembers().get("start").getClass());
        Assertions.assertEquals(EndsWithValidationMatcher.class, matcherLibraryBean.getMembers().get("end").getClass());
        Assertions.assertEquals(CustomValidationMatcher.class, matcherLibraryBean.getMembers().get("custom").getClass());

        matcherLibraryBean.getMembers().get("custom").validate("field", "Hello Citrus!", singletonList("Hello Citrus!"), context);

        matcherLibraryBean = matcherLibraries.get("matcherLib2");
        Assertions.assertEquals("matcherLib2", matcherLibraryBean.getName());
        Assertions.assertEquals("bar", matcherLibraryBean.getPrefix());
        Assertions.assertEquals(2L, matcherLibraryBean.getMembers().size());
        Assertions.assertEquals(IsNumberValidationMatcher.class, matcherLibraryBean.getMembers().get("isNumber").getClass());
        Assertions.assertEquals(CustomValidationMatcher.class, matcherLibraryBean.getMembers().get("custom").getClass());

        matcherLibraryBean.getMembers().get("custom").validate("field", "Hello Citrus!", singletonList("Hello Citrus!"), context);
    }
}
