package com.consol.citrus.validation.matcher;

import com.consol.citrus.validation.matcher.core.ContainsIgnoreCaseValidationMatcher;
import com.consol.citrus.validation.matcher.core.ContainsValidationMatcher;
import com.consol.citrus.validation.matcher.core.CreateVariableValidationMatcher;
import com.consol.citrus.validation.matcher.core.DatePatternValidationMatcher;
import com.consol.citrus.validation.matcher.core.DateRangeValidationMatcher;
import com.consol.citrus.validation.matcher.core.EmptyValidationMatcher;
import com.consol.citrus.validation.matcher.core.EndsWithValidationMatcher;
import com.consol.citrus.validation.matcher.core.EqualsIgnoreCaseValidationMatcher;
import com.consol.citrus.validation.matcher.core.GreaterThanValidationMatcher;
import com.consol.citrus.validation.matcher.core.IgnoreNewLineValidationMatcher;
import com.consol.citrus.validation.matcher.core.IgnoreValidationMatcher;
import com.consol.citrus.validation.matcher.core.IsNumberValidationMatcher;
import com.consol.citrus.validation.matcher.core.LowerThanValidationMatcher;
import com.consol.citrus.validation.matcher.core.MatchesValidationMatcher;
import com.consol.citrus.validation.matcher.core.NotEmptyValidationMatcher;
import com.consol.citrus.validation.matcher.core.NotNullValidationMatcher;
import com.consol.citrus.validation.matcher.core.NullValidationMatcher;
import com.consol.citrus.validation.matcher.core.StartsWithValidationMatcher;
import com.consol.citrus.validation.matcher.core.StringLengthValidationMatcher;
import com.consol.citrus.validation.matcher.core.TrimAllWhitespacesValidationMatcher;
import com.consol.citrus.validation.matcher.core.TrimValidationMatcher;
import com.consol.citrus.validation.matcher.core.WeekdayValidationMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
public class DefaultValidationMatcherLibrary extends ValidationMatcherLibrary {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DefaultValidationMatcherLibrary.class);

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

        lookupValidationMatchers();
    }

    /**
     * Add custom matcher implementations loaded from resource path lookup.
     */
    private void lookupValidationMatchers() {
        ValidationMatcher.lookup().forEach((k, m) -> {
            getMembers().put(k, m);
            log.info(String.format("Register message matcher '%s' as %s", k, m.getClass()));
        });
    }
}
