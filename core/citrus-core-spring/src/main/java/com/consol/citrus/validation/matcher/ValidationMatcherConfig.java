/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.validation.matcher;

import com.consol.citrus.validation.matcher.core.*;
import com.consol.citrus.validation.matcher.hamcrest.HamcrestMatcherProvider;
import com.consol.citrus.validation.matcher.hamcrest.HamcrestValidationMatcher;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Configuration
public class ValidationMatcherConfig {

    private final XmlValidationMatcher xmlValidationMatcher = new XmlValidationMatcher();
    private final HamcrestValidationMatcher hamcrestValidationMatcher = new HamcrestValidationMatcher();

    private final ValidationMatcherLibrary citrusValidationMatcherLibrary = new DefaultValidationMatcherLibrary();

    @Bean(name = "matchesPath")
    public HamcrestMatcherProvider matchesPath() {
        return new HamcrestMatcherProvider() {
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
        };
    }

    @Bean
    public HamcrestValidationMatcher hamcrestValidationMatcher() {
        return hamcrestValidationMatcher;
    }

    @Bean(name = "validationMatcherRegistry")
    public ValidationMatcherRegistry getValidationMatcherRegistry() {
        return new ValidationMatcherRegistry();
    }

    @Bean(name = "xmlValidationMatcher")
    public XmlValidationMatcher getXmlValidationMatcher() {
        return xmlValidationMatcher;
    }

    @Bean(name = "citrusValidationMatcherLibrary")
    public ValidationMatcherLibrary getValidationMatcherLibrary() {
        citrusValidationMatcherLibrary.getMembers().put("matchesXml", xmlValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("assertThat", hamcrestValidationMatcher());

        return citrusValidationMatcherLibrary;
    }
}
