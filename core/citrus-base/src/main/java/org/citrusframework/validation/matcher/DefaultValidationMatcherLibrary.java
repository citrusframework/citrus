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

package org.citrusframework.validation.matcher;

import org.citrusframework.validation.matcher.core.ContainsIgnoreCaseValidationMatcher;
import org.citrusframework.validation.matcher.core.ContainsValidationMatcher;
import org.citrusframework.validation.matcher.core.CreateVariableValidationMatcher;
import org.citrusframework.validation.matcher.core.DatePatternValidationMatcher;
import org.citrusframework.validation.matcher.core.DateRangeValidationMatcher;
import org.citrusframework.validation.matcher.core.EmptyValidationMatcher;
import org.citrusframework.validation.matcher.core.EndsWithValidationMatcher;
import org.citrusframework.validation.matcher.core.EqualsIgnoreCaseValidationMatcher;
import org.citrusframework.validation.matcher.core.GreaterThanValidationMatcher;
import org.citrusframework.validation.matcher.core.IgnoreNewLineValidationMatcher;
import org.citrusframework.validation.matcher.core.IgnoreValidationMatcher;
import org.citrusframework.validation.matcher.core.IsNumberValidationMatcher;
import org.citrusframework.validation.matcher.core.LowerThanValidationMatcher;
import org.citrusframework.validation.matcher.core.MatchesValidationMatcher;
import org.citrusframework.validation.matcher.core.NotEmptyValidationMatcher;
import org.citrusframework.validation.matcher.core.NotNullValidationMatcher;
import org.citrusframework.validation.matcher.core.NullValidationMatcher;
import org.citrusframework.validation.matcher.core.StartsWithValidationMatcher;
import org.citrusframework.validation.matcher.core.StringLengthValidationMatcher;
import org.citrusframework.validation.matcher.core.TrimAllWhitespacesValidationMatcher;
import org.citrusframework.validation.matcher.core.TrimValidationMatcher;
import org.citrusframework.validation.matcher.core.UuidV4ValidationMatcher;
import org.citrusframework.validation.matcher.core.WeekdayValidationMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultValidationMatcherLibrary extends ValidationMatcherLibrary {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(DefaultValidationMatcherLibrary.class);

    /**
     * Default constructor adds default matcher implementations.
     */
    public DefaultValidationMatcherLibrary() {
        setName("citrusValidationMatcherLibrary");

        getMembers().put("equalsIgnoreCase", new EqualsIgnoreCaseValidationMatcher());
        getMembers().put("ignoreNewLine", new IgnoreNewLineValidationMatcher());
        getMembers().put("trim", new TrimValidationMatcher());
        getMembers().put("trimAllWhitespaces", new TrimAllWhitespacesValidationMatcher());
        getMembers().put("contains", new ContainsValidationMatcher());
        getMembers().put("containsIgnoreCase", new ContainsIgnoreCaseValidationMatcher());
        getMembers().put("greaterThan", new GreaterThanValidationMatcher());
        getMembers().put("lowerThan", new LowerThanValidationMatcher());
        getMembers().put("startsWith", new StartsWithValidationMatcher());
        getMembers().put("endsWith", new EndsWithValidationMatcher());
        getMembers().put("isNumber", new IsNumberValidationMatcher());
        getMembers().put("matches", new MatchesValidationMatcher());
        getMembers().put("matchesDatePattern", new DatePatternValidationMatcher());
        getMembers().put("isWeekday", new WeekdayValidationMatcher());
        getMembers().put("variable", new CreateVariableValidationMatcher());
        getMembers().put("dateRange", new DateRangeValidationMatcher());
        getMembers().put("empty", new EmptyValidationMatcher());
        getMembers().put("notEmpty", new NotEmptyValidationMatcher());
        getMembers().put("null", new NullValidationMatcher());
        getMembers().put("notNull", new NotNullValidationMatcher());
        getMembers().put("ignore", new IgnoreValidationMatcher());
        getMembers().put("hasLength", new StringLengthValidationMatcher());
        getMembers().put("isUUIDv4", new UuidV4ValidationMatcher());

        lookupValidationMatchers();
    }

    /**
     * Add custom matcher implementations loaded from resource path lookup.
     */
    private void lookupValidationMatchers() {
        ValidationMatcher.lookup().forEach((k, m) -> {
            getMembers().put(k, m);
            logger.debug("Register message matcher '{}' as {}", k, m.getClass());
        });
    }
}
