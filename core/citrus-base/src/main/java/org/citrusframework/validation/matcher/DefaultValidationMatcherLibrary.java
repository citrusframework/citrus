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

import org.citrusframework.CitrusSettings;
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

        addMember("equalsIgnoreCase", new EqualsIgnoreCaseValidationMatcher());
        addMember("ignoreNewLine", new IgnoreNewLineValidationMatcher());
        addMember("trim", new TrimValidationMatcher());
        addMember("trimAllWhitespaces", new TrimAllWhitespacesValidationMatcher());
        addMember("contains", new ContainsValidationMatcher());
        addMember("containsIgnoreCase", new ContainsIgnoreCaseValidationMatcher());
        addMember("greaterThan", new GreaterThanValidationMatcher());
        addMember("lowerThan", new LowerThanValidationMatcher());
        addMember("startsWith", new StartsWithValidationMatcher());
        addMember("endsWith", new EndsWithValidationMatcher());
        addMember("isNumber", new IsNumberValidationMatcher());
        addMember("matches", new MatchesValidationMatcher());
        addMember("matchesDatePattern", new DatePatternValidationMatcher());
        addMember("isWeekday", new WeekdayValidationMatcher());
        addMember("variable", new CreateVariableValidationMatcher());
        addMember("dateRange", new DateRangeValidationMatcher());
        addMember("empty", new EmptyValidationMatcher());
        addMember("notEmpty", new NotEmptyValidationMatcher());
        addMember("null", new NullValidationMatcher());
        addMember("notNull", new NotNullValidationMatcher());
        addMember("ignore", new IgnoreValidationMatcher());
        addMember("hasLength", new StringLengthValidationMatcher());
        addMember("isUUIDv4", new UuidV4ValidationMatcher());

        lookupValidationMatchers();
    }

    /**
     * Add custom matcher implementations loaded from resource path lookup.
     */
    private void lookupValidationMatchers() {
        boolean allowOverride = CitrusSettings.isAllowValidationMatcherOverride();

        ValidationMatcher.lookup().forEach((k, m) -> {
            if (allowOverride) {
                getMembers().put(k, m);
            } else {
                addMember(k, m);
            }

            logger.trace("Register validation matcher '{}' as {}", k, m.getClass());
        });
    }
}
