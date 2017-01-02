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
import com.consol.citrus.validation.matcher.hamcrest.HamcrestValidationMatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Configuration
public class ValidationMatcherConfig {

    private final ContainsIgnoreCaseValidationMatcher containsIgnoreCaseValidationMatcher = new ContainsIgnoreCaseValidationMatcher();
    private final EqualsIgnoreCaseValidationMatcher equalsIgnoreCaseValidationMatcher = new EqualsIgnoreCaseValidationMatcher();
    private final ContainsValidationMatcher containsValidationMatcher = new ContainsValidationMatcher();
    private final GreaterThanValidationMatcher greaterThanValidationMatcher = new GreaterThanValidationMatcher();
    private final LowerThanValidationMatcher lowerThanValidationMatcher = new LowerThanValidationMatcher();
    private final StartsWithValidationMatcher startsWithValidationMatcher = new StartsWithValidationMatcher();
    private final EndsWithValidationMatcher endsWithValidationMatcher = new EndsWithValidationMatcher();
    private final IsNumberValidationMatcher isNumberValidationMatcher = new IsNumberValidationMatcher();
    private final MatchesValidationMatcher matchesValidationMatcher = new MatchesValidationMatcher();
    private final DatePatternValidationMatcher datePatternValidationMatcher = new DatePatternValidationMatcher();
    private final XmlValidationMatcher xmlValidationMatcher = new XmlValidationMatcher();
    private final WeekdayValidationMatcher weekDayValidationMatcher = new WeekdayValidationMatcher();
    private final CreateVariableValidationMatcher createVariablesValidationMatcher = new CreateVariableValidationMatcher();
    private final DateRangeValidationMatcher dateRangeValidationMatcher = new DateRangeValidationMatcher();
    private final HamcrestValidationMatcher hamcrestValidationMatcher = new HamcrestValidationMatcher();
    private final EmptyValidationMatcher emptyValidationMatcher = new EmptyValidationMatcher();
    private final NotEmptyValidationMatcher notEmptyValidationMatcher = new NotEmptyValidationMatcher();
    private final NullValidationMatcher nullValidationMatcher = new NullValidationMatcher();
    private final NotNullValidationMatcher notNullValidationMatcher = new NotNullValidationMatcher();

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
        ValidationMatcherLibrary citrusValidationMatcherLibrary = new ValidationMatcherLibrary();

        citrusValidationMatcherLibrary.setPrefix("");
        citrusValidationMatcherLibrary.setName("citrusValidationMatcherLibrary");

        citrusValidationMatcherLibrary.getMembers().put("equalsIgnoreCase", equalsIgnoreCaseValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("contains", containsValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("containsIgnoreCase", containsIgnoreCaseValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("greaterThan", greaterThanValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("lowerThan", lowerThanValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("startsWith", startsWithValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("endsWith", endsWithValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("isNumber", isNumberValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("matches", matchesValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("matchesDatePattern", datePatternValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("matchesXml", xmlValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("isWeekday", weekDayValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("variable", createVariablesValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("dateRange", dateRangeValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("assertThat", hamcrestValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("empty", emptyValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("notEmpty", notEmptyValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("null", nullValidationMatcher);
        citrusValidationMatcherLibrary.getMembers().put("notNull", notNullValidationMatcher);

        return citrusValidationMatcherLibrary;
    }
}
