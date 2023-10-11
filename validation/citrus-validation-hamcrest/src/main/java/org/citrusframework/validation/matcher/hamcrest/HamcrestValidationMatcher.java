/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.validation.matcher.hamcrest;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.util.ReflectionHelper;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.matcher.ControlExpressionParser;
import org.citrusframework.validation.matcher.DefaultControlExpressionParser;
import org.citrusframework.validation.matcher.ValidationMatcher;
import org.citrusframework.variable.VariableUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@SuppressWarnings("unchecked")
public class HamcrestValidationMatcher implements ValidationMatcher, ControlExpressionParser {

    private final List<String> matchers = Arrays.asList( "equalTo", "equalToIgnoringCase", "equalToIgnoringWhiteSpace", "is", "not", "containsString", "startsWith", "endsWith", "matchesPattern" );

    private final List<String> collectionMatchers = Arrays.asList("hasSize", "hasItem", "hasItems", "contains", "containsInAnyOrder");

    private final List<String> mapMatchers = Arrays.asList("hasEntry", "hasKey", "hasValue");

    private final List<String> optionMatchers = Arrays.asList("isOneOf", "isIn");

    private final List<String> numericMatchers = Arrays.asList( "greaterThan", "greaterThanOrEqualTo", "lessThan", "lessThanOrEqualTo", "closeTo" );

    private final List<String> containerMatchers = Arrays.asList( "is", "not", "everyItem" );

    private final List<String> noArgumentMatchers = Arrays.asList( "isEmptyString", "isEmptyOrNullString", "nullValue", "notNullValue", "anything" );

    private final List<String> noArgumentCollectionMatchers = Collections.singletonList("empty");

    private final List<String> iterableMatchers = Arrays.asList( "anyOf", "allOf" );

    @Override
    public void validate(String fieldName, String value, List<String> controlParameters, TestContext context) throws ValidationException {
        String matcherExpression;
        String matcherValue = value;

        if (controlParameters.size() > 1) {
            matcherValue = context.replaceDynamicContentInString(controlParameters.get(0));
            matcherExpression = controlParameters.get(1);
        } else {
            matcherExpression = controlParameters.get(0);
        }

        String matcherName = matcherExpression.trim().substring(0, matcherExpression.trim().indexOf("("));

        String[] matcherParameter = determineNestedMatcherParameters(matcherExpression.trim()
            .substring(matcherName.length() + 1, matcherExpression.trim().length() - 1));

        try {
            Matcher matcher = getMatcher(matcherName, matcherParameter, context);
            if (noArgumentCollectionMatchers.contains(matcherName) ||
                    collectionMatchers.contains(matcherName) ||
                    matcherName.equals("everyItem")) {
                assertThat(getCollection(matcherValue), matcher);
            } else if (mapMatchers.contains(matcherName)) {
                assertThat(getMap(matcherValue), matcher);
            } else if (numericMatchers.contains(matcherName)) {
                if (matcherName.equals("closeTo")) {
                    assertThat(Double.valueOf(matcherValue), matcher);
                } else {
                    assertThat(new NumericComparable(matcherValue), matcher);
                }
            } else if (iterableMatchers.contains(matcherName) && containsNumericMatcher(matcherExpression)) {
                assertThat(new NumericComparable(matcherValue), matcher);
            } else {
                assertThat(matcherValue, matcher);
            }
        } catch (AssertionError e) {
            throw new ValidationException(this.getClass().getSimpleName()
                    + " failed for field '" + fieldName
                    + "'. Received value is '" + value
                    + "' and did not match '" + matcherExpression + "'.", e);
        }
    }

    /**
     * Construct matcher by name and parameters.
     * @param matcherName
     * @param matcherParameter
     * @param context
     * @return
     */
    private Matcher<?> getMatcher(String matcherName, String[] matcherParameter, TestContext context) {

        try {
            if (context.getReferenceResolver().isResolvable(matcherName, HamcrestMatcherProvider.class) ||
                    HamcrestMatcherProvider.canResolve(matcherName)) {
                Optional<HamcrestMatcherProvider> matcherProvider = lookupMatcherProvider(matcherName, context);
                if (matcherProvider.isPresent()) {
                    return matcherProvider.get().provideMatcher(matcherParameter[0]);
                }
            }

            if (noArgumentMatchers.contains(matcherName)) {
                Method matcherMethod = ReflectionHelper.findMethod(Matchers.class, matcherName);

                if (matcherMethod != null) {
                    return (Matcher<?>) matcherMethod.invoke(null);
                }
            }

            if (noArgumentCollectionMatchers.contains(matcherName)) {
                Method matcherMethod = ReflectionHelper.findMethod(Matchers.class, matcherName);

                if (matcherMethod != null) {
                    return (Matcher<?>) matcherMethod.invoke(null);
                }
            }

            if (matcherParameter.length == 0) {
                throw new CitrusRuntimeException("Missing matcher parameter");
            }

            if (containerMatchers.contains(matcherName)) {
                Method matcherMethod = ReflectionHelper.findMethod(Matchers.class, matcherName, Matcher.class);

                if (matcherMethod != null) {
                    String matcherExpression = matcherParameter[0];

                    if (matcherExpression.contains("(") && matcherExpression.contains(")")) {
                        String nestedMatcherName = matcherExpression.trim().substring(0, matcherExpression.trim().indexOf("("));
                        String[] nestedMatcherParameter = matcherExpression.trim()
                            .substring(
                                nestedMatcherName.length() + 1,
                                matcherExpression.trim().length() - 1)
                            .split(",");

                        return (Matcher<?>) matcherMethod.invoke(null, getMatcher(nestedMatcherName, nestedMatcherParameter,context));
                    }
                }
            }

            if (iterableMatchers.contains(matcherName)) {
                Method matcherMethod = ReflectionHelper.findMethod(Matchers.class, matcherName, Iterable.class);

                if (matcherMethod != null) {
                    List<Matcher<?>> nestedMatchers = new ArrayList<>();
                    for (String matcherExpression : matcherParameter) {
                        String nestedMatcherName = matcherExpression.trim().substring(0, matcherExpression.trim().indexOf("("));
                        String[] nestedMatcherParameters = determineNestedMatcherParameters(
                            matcherExpression.trim().
                                substring(
                                    nestedMatcherName.length() + 1,
                                    matcherExpression.trim().length() - 1));

                        nestedMatchers.add(getMatcher(nestedMatcherName, nestedMatcherParameters, context));
                    }

                    return (Matcher<?>) matcherMethod.invoke(null, nestedMatchers);
                }
            }

            if (matchers.contains(matcherName)) {

                unescapeQuotes(matcherParameter);

                Method matcherMethod = ReflectionHelper.findMethod(Matchers.class, matcherName, String.class);

                if (matcherMethod == null) {
                    matcherMethod =  ReflectionHelper.findMethod(Matchers.class, matcherName, Object.class);
                }

                if (matcherMethod != null) {
                    return (Matcher<?>) matcherMethod.invoke(null, matcherParameter[0]);
                }
            }

            if (numericMatchers.contains(matcherName)) {
                Method matcherMethod = ReflectionHelper.findMethod(Matchers.class, matcherName, double.class, double.class);

                if (matcherMethod != null) {
                    return (Matcher<?>) matcherMethod.invoke(
                        null,
                        Double.valueOf(matcherParameter[0]), matcherParameter.length > 1 ? Double.parseDouble(matcherParameter[1]) : 0.0D);
                }

                matcherMethod = ReflectionHelper.findMethod(Matchers.class, matcherName, Comparable.class);

                if (matcherMethod != null) {
                    return (Matcher<?>) matcherMethod.invoke(null, matcherParameter[0]);
                }
            }

            if (collectionMatchers.contains(matcherName)) {

                unescapeQuotes(matcherParameter);

                Method matcherMethod = ReflectionHelper.findMethod(Matchers.class, matcherName, int.class);

                if (matcherMethod != null) {
                    return (Matcher<?>) matcherMethod.invoke(null, Integer.valueOf(matcherParameter[0]));
                }

                matcherMethod =  ReflectionHelper.findMethod(Matchers.class, matcherName, Object.class);

                if (matcherMethod != null) {
                    return (Matcher<?>) matcherMethod.invoke(null, matcherParameter[0]);
                }

                matcherMethod =  ReflectionHelper.findMethod(Matchers.class, matcherName, Object[].class);

                if (matcherMethod != null) {
                    return (Matcher<?>) matcherMethod.invoke(null, new Object[] { matcherParameter });
                }
            }

            if (mapMatchers.contains(matcherName)) {

                unescapeQuotes(matcherParameter);

                Method matcherMethod =  ReflectionHelper.findMethod(Matchers.class, matcherName, Object.class);

                if (matcherMethod != null) {
                    return (Matcher<?>) matcherMethod.invoke(null, matcherParameter[0]);
                }

                matcherMethod =  ReflectionHelper.findMethod(Matchers.class, matcherName, Object.class, Object.class);

                if (matcherMethod != null) {
                    return (Matcher<?>) matcherMethod.invoke(null, matcherParameter[0], matcherParameter[1]);
                }
            }

            if (optionMatchers.contains(matcherName)) {

                unescapeQuotes(matcherParameter);

                Method matcherMethod =  ReflectionHelper.findMethod(Matchers.class, matcherName, Object[].class);

                if (matcherMethod != null) {
                    return (Matcher<?>) matcherMethod.invoke(null, new Object[] { matcherParameter });
                }

                matcherMethod =  ReflectionHelper.findMethod(Matchers.class, matcherName, Collection.class);

                if (matcherMethod != null) {
                    return (Matcher<?>) matcherMethod.invoke(
                        null,
                        new Object[] { getCollection(String.join(",", matcherParameter)) });
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new CitrusRuntimeException("Failed to invoke matcher", e);
        }

        throw new CitrusRuntimeException("Unsupported matcher: " + matcherName);
    }

    /**
     * Unescape the quotes in search expressions  (\\' -> ').
     * @param matcherParameters to unescape
     */
    private static void unescapeQuotes(String[] matcherParameters) {
        if (matcherParameters != null) {
            for (int i=0; i< matcherParameters.length; i++) {
                matcherParameters[i] = matcherParameters[i].replace("\\'","'");
            }
        }
    }

    /**
     * Try to find matcher provider using different lookup strategies. Looks into reference resolver and resource path for matcher provider.
     * @param matcherName
     * @param context
     * @return
     */
    private Optional<HamcrestMatcherProvider> lookupMatcherProvider(String matcherName, TestContext context) {
        // try to find matcher provider via reference
        Optional<HamcrestMatcherProvider> matcherProvider = context.getReferenceResolver()
            .resolveAll(HamcrestMatcherProvider.class)
                .values()
                .stream()
                .filter(provider -> provider.getName().equals(matcherName))
                .findFirst();

        if (!matcherProvider.isPresent()) {
            // try to resolve via resource path lookup
            matcherProvider = HamcrestMatcherProvider.lookup(matcherName);
        }

        return matcherProvider;
    }

    /**
     * Construct collection from delimited string expression.
     * @param value
     * @return
     */
    private List<String> getCollection(String value) {
        if (value.equals("[]")) {
            return Collections.emptyList();
        }

        String arrayString = value;
        if (arrayString.startsWith("[") && arrayString.endsWith("]")) {
            arrayString = arrayString.substring(1, arrayString.length()-1);
        }

        return Arrays.stream(arrayString.split(","))
                .map(String::trim)
                .map(VariableUtils::cutOffDoubleQuotes)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    /**
     * Construct collection from delimited string expression.
     * @param mapString
     * @return
     */
    private Map<String, Object> getMap(String mapString) {
        Properties props = new Properties();

        try {
            props.load(new StringReader(mapString.substring(1, mapString.length() - 1).replaceAll(",\\s*", "\n")));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to reconstruct object of type map", e);
        }

        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String key;
            Object value;
            if (entry.getKey() instanceof String) {
                key = VariableUtils.cutOffDoubleQuotes(entry.getKey().toString());
            } else {
                key = entry.getKey().toString();
            }

            if (entry.getValue() instanceof String) {
                value = VariableUtils.cutOffDoubleQuotes(entry.getValue().toString()).trim();
            } else {
                value = entry.getValue();
            }

            map.put(key, value);
        }

        return map;
    }

    /**
     * Checks for numeric matcher presence in expression.
     * @param matcherExpression
     * @return
     */
    private boolean containsNumericMatcher(String matcherExpression) {
        for (String numericMatcher : numericMatchers) {
            if (matcherExpression.contains(numericMatcher)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<String> extractControlValues(String controlExpression, Character delimiter) {
        if (controlExpression.startsWith("'") && controlExpression.contains("',")) {
            return new DefaultControlExpressionParser().extractControlValues(controlExpression, delimiter);
        } else {
            return Collections.singletonList(controlExpression);
        }
    }

    /**
     * Extracts parameters for a matcher from the raw parameter expression.
     * Parameters refer to the contained parameters and matchers (first level),
     * excluding nested ones.
     * <p/>
     * For example, given the expression:<br/>
     * {@code "oneOf(greaterThan(5.0), allOf(lessThan(-1.0), greaterThan(-2.0)))"}
     * <p/>
     * The extracted parameters are:<br/>
     * {@code "greaterThan(5.0)", "allOf(lessThan(-1.0), greaterThan(-2.0))"}.
     * <p/>
     * Note that nested container expressions "allOf(lessThan(-1.0), greaterThan(-2.0))" in
     * the second parameter are treated as a single expression. They need to be treated
     * separately in a recursive call to this method, when the parameters for the
     * respective allOf() expression are extracted.
     *
     * @param rawExpression the full parameter expression of a container matcher
     */
    public String[] determineNestedMatcherParameters(final String rawExpression) {
        if (!StringUtils.hasText(rawExpression)) {
            return new String[0];
        }

        Tokenizer tokenizer = new Tokenizer();
        String tokenizedExpression = tokenizer.tokenize(rawExpression);
        return tokenizer.restoreInto(tokenizedExpression.split(","));

    }

    /**
     * Numeric value comparable automatically converts types to numeric values for
     * comparison.
     */
    private static class NumericComparable implements Comparable<Object> {

        private Long number = null;
        private Double decimal = null;

        /**
         * Constructor initializing numeric value from string.
         * @param value
         */
        public NumericComparable(String value) {
            if (value.contains(".")) {
                this.decimal = Double.parseDouble(value);
            } else {
                try {
                    this.number = Long.parseLong(value);
                } catch (NumberFormatException e) {
                    throw new AssertionError(e);
                }
            }
        }

        @Override
        public int compareTo(Object o) {
            if (number != null) {
                if (o instanceof String || o instanceof NumericComparable) {
                    return number.compareTo(Long.parseLong(o.toString()));
                } else if (o instanceof Long) {
                    return number.compareTo((Long) o);
                }
            }

            if (decimal != null) {
                if (o instanceof String || o instanceof NumericComparable) {
                    return decimal.compareTo(Double.parseDouble(o.toString()));
                } else if (o instanceof Double) {
                    return decimal.compareTo((Double) o);
                }
            }

            return 0;
        }

        @Override
        public String toString() {
            if (number != null) {
                return number.toString();
            } else {
                return decimal.toString();
            }
        }
    }

    /**
     * Class that provides functionality to replace expressions that match
     * {@link Tokenizer#TEXT_PARAMETER_PATTERN} with simple tokens of the form $$n$$.
     * The reason for this is, that complex nested expressions
     * may contain characters that interfere with further processing - e.g. ''', '(' and ')'
     */
    private static class Tokenizer {

        private static final String START_TOKEN = "_TOKEN-";

        private static final String END_TOKEN = "-TOKEN_";

        /**
         * Regular expression with three alternative parts (ored) to match:
         * <ol>
         *   <li> ('sometext') - Quoted parameter block of a matcher.</li>
         *   <li> 'sometext' - Quoted text used as a parameter to a string matcher.</li>
         *   <li> (unquotedtext) - Unquoted text used as a parameter to a string matcher. This expression is non-greedy, meaning the first closing bracket will terminate the match.</li>
         * </ol>
         * <p/>
         * Please note:
         * - 'sometext' may contain an escaped quote.
         * - 'unquotedtext' must not contain brackets or commas.
         * <p/>
         * To match quotes, commas, or brackets, you must quote the text. To match a quote, it should be escaped with a backslash.
         * Therefore, the regex expressions explicitly match the escaped quote -> \\\\'
         */
        private static final Pattern TEXT_PARAMETER_PATTERN = Pattern.compile(
                "(?<quoted1>\\('(?:[^']|\\\\')*[^\\\\]'\\))"
                + "|(?<quoted2>('(?:[^']|\\\\')*[^\\\\]'))"
                + "|(?<unquoted>\\(((?:[^']|\\\\')*?)[^\\\\]?\\))"
        );

        private final List<String> originalTokenValues = new ArrayList<>();

        /**
         * Tokenize the given raw expression
         *
         * @param rawExpression
         * @return the expression with all relevant subexpressions replaced by tokens
         */
        public String tokenize(String rawExpression) {
            java.util.regex.Matcher matcher = TEXT_PARAMETER_PATTERN.matcher(rawExpression);
            StringBuilder builder = new StringBuilder();

            while (matcher.find()) {
                String matchedValue = findMatchedValue(matcher);
                originalTokenValues.add(matchedValue);
                matcher.appendReplacement(builder, START_TOKEN + originalTokenValues.size() + END_TOKEN);
            }

            matcher.appendTail(builder);
            return builder.toString();
        }

        /**
         * @param matcher the matcher that was used to match
         * @return the value of the group, that was actually matched
         */
        private String findMatchedValue(java.util.regex.Matcher matcher) {
            String matchedValue = matcher.group("quoted1");
            matchedValue = matchedValue != null ? matchedValue : matcher.group("quoted2");
            return matchedValue != null ? matchedValue : matcher.group("unquoted");
        }

        /**
         * Restore the tokens back into the given expressions.
         *
         * @param expressions containing strings with tokens, generated by this tokenizer.
         * @return expressions with the tokens being replaced with their original values.
         */
        public String[] restoreInto(String[] expressions) {

            for (int i = 0; i < expressions.length; i++) {
                expressions[i] = VariableUtils.cutOffSingleQuotes(
                    replaceTokens(expressions[i], originalTokenValues).trim());
            }

            return expressions;
        }

        private String replaceTokens(String expression, List<String> params) {
            for (int i = 0; i < params.size(); i++) {
                expression = expression.replace(START_TOKEN + (i + 1) + END_TOKEN, params.get(i));
            }
            return expression;
        }
    }
}
