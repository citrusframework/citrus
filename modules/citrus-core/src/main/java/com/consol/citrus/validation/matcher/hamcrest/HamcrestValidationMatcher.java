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

package com.consol.citrus.validation.matcher.hamcrest;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.validation.matcher.*;
import com.consol.citrus.variable.VariableUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.springframework.util.*;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@SuppressWarnings("unchecked")
public class HamcrestValidationMatcher implements ValidationMatcher, ControlExpressionParser {

    private List<String> matchers = Arrays.asList( "equalTo", "equalToIgnoringCase", "equalToIgnoringWhiteSpace", "is", "not", "containsString", "startsWith", "endsWith" );

    private List<String> collectionMatchers = Arrays.asList("hasSize", "hasItem", "hasItems", "contains", "containsInAnyOrder");

    private List<String> mapMatchers = Arrays.asList("hasEntry", "hasKey", "hasValue");

    private List<String> numericMatchers = Arrays.asList( "greaterThan", "greaterThanOrEqualTo", "lessThan", "lessThanOrEqualTo" );

    private List<String> containerMatchers = Arrays.asList( "is", "not", "everyItem" );

    private List<String> noArgumentMatchers = Arrays.asList( "isEmptyString", "isEmptyOrNullString", "nullValue", "notNullValue", "anything" );

    private List<String> noArgumentCollectionMatchers = Collections.singletonList("empty");

    private List<String> iterableMatchers = Arrays.asList( "anyOf", "allOf" );

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
        String[] matcherParameter = matcherExpression.trim().substring(matcherName.length() + 1, matcherExpression.trim().length() - 1).split(",");

        for (int i = 0; i < matcherParameter.length; i++) {
            matcherParameter[i] = VariableUtils.cutOffSingleQuotes(matcherParameter[i]);
        }

        Matcher matcher = getMatcher(matcherName, matcherParameter);
        if (noArgumentCollectionMatchers.contains(matcherName) ||
                collectionMatchers.contains(matcherName) ||
                matcherName.equals("everyItem")) {
            assertThat(getCollection(matcherValue), matcher);
        } else if (mapMatchers.contains(matcherName)) {
            assertThat(getMap(matcherValue), matcher);
        } else if (numericMatchers.contains(matcherName)) {
            assertThat(new NumericComparable(matcherValue), matcher);
        } else if (iterableMatchers.contains(matcherName) && containsNumericMatcher(matcherExpression)) {
            assertThat(new NumericComparable(matcherValue), matcher);
        } else {
            assertThat(matcherValue, matcher);
        }
    }

    /**
     * Construct matcher by name and parameters.
     * @param matcherName
     * @param matcherParameter
     * @return
     */
    private Matcher getMatcher(String matcherName, String[] matcherParameter) {
        try {
            if (noArgumentMatchers.contains(matcherName)) {
                Method matcherMethod = ReflectionUtils.findMethod(Matchers.class, matcherName);

                if (matcherMethod != null) {
                    return (Matcher) matcherMethod.invoke(null);
                }
            }

            if (noArgumentCollectionMatchers.contains(matcherName)) {
                Method matcherMethod = ReflectionUtils.findMethod(Matchers.class, matcherName);

                if (matcherMethod != null) {
                    return (Matcher) matcherMethod.invoke(null);
                }
            }

            Assert.isTrue(matcherParameter.length > 0, "Missing matcher parameter");

            if (containerMatchers.contains(matcherName)) {
                Method matcherMethod = ReflectionUtils.findMethod(Matchers.class, matcherName, Matcher.class);

                if (matcherMethod != null) {
                    String matcherExpression = matcherParameter[0];

                    if (matcherExpression.contains("(") && matcherExpression.contains(")")) {
                        String nestedMatcherName = matcherExpression.trim().substring(0, matcherExpression.trim().indexOf("("));
                        String[] nestedMatcherParameter = matcherExpression.trim().substring(nestedMatcherName.length() + 1, matcherExpression.trim().length() - 1).split(",");

                        return (Matcher) matcherMethod.invoke(null, getMatcher(nestedMatcherName, nestedMatcherParameter));
                    }
                }
            }

            if (iterableMatchers.contains(matcherName)) {
                Method matcherMethod = ReflectionUtils.findMethod(Matchers.class, matcherName, Iterable.class);

                if (matcherMethod != null) {
                    List<Matcher> nestedMatchers = new ArrayList<>();
                    for (String matcherExpression : matcherParameter) {
                        String nestedMatcherName = matcherExpression.trim().substring(0, matcherExpression.trim().indexOf("("));
                        String nestedMatcherParameter = matcherExpression.trim().substring(nestedMatcherName.length() + 1, matcherExpression.trim().length() - 1);
                        nestedMatchers.add(getMatcher(nestedMatcherName, new String[] { nestedMatcherParameter }));
                    }

                    return (Matcher) matcherMethod.invoke(null, nestedMatchers);
                }
            }

            if (matchers.contains(matcherName)) {
                Method matcherMethod = ReflectionUtils.findMethod(Matchers.class, matcherName, String.class);

                if (matcherMethod == null) {
                    matcherMethod =  ReflectionUtils.findMethod(Matchers.class, matcherName, Object.class);
                }

                if (matcherMethod != null) {
                    return (Matcher) matcherMethod.invoke(null, matcherParameter[0]);
                }
            }

            if (numericMatchers.contains(matcherName)) {
                Method matcherMethod = ReflectionUtils.findMethod(Matchers.class, matcherName, Comparable.class);

                if (matcherMethod != null) {
                    return (Matcher) matcherMethod.invoke(null, matcherParameter[0]);
                }
            }

            if (collectionMatchers.contains(matcherName)) {
                Method matcherMethod = ReflectionUtils.findMethod(Matchers.class, matcherName, int.class);

                if (matcherMethod != null) {
                    return (Matcher) matcherMethod.invoke(null, Integer.valueOf(matcherParameter[0]));
                }

                matcherMethod =  ReflectionUtils.findMethod(Matchers.class, matcherName, Object.class);

                if (matcherMethod != null) {
                    return (Matcher) matcherMethod.invoke(null, matcherParameter[0]);
                }

                matcherMethod =  ReflectionUtils.findMethod(Matchers.class, matcherName, Object[].class);

                if (matcherMethod != null) {
                    return (Matcher) matcherMethod.invoke(null, new Object[] { matcherParameter });
                }
            }

            if (mapMatchers.contains(matcherName)) {
                Method matcherMethod =  ReflectionUtils.findMethod(Matchers.class, matcherName, Object.class);

                if (matcherMethod != null) {
                    return (Matcher) matcherMethod.invoke(null, matcherParameter[0]);
                }

                matcherMethod =  ReflectionUtils.findMethod(Matchers.class, matcherName, Object.class, Object.class);

                if (matcherMethod != null) {
                    return (Matcher) matcherMethod.invoke(null, matcherParameter[0], matcherParameter[1]);
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new CitrusRuntimeException("Failed to invoke matcher", e);
        }

        throw new CitrusRuntimeException("Unsupported matcher: " + matcherName);
    }

    /**
     * Construct collection from delimited string expression.
     * @param value
     * @return
     */
    private List<String> getCollection(String value) {
        String arrayString = value;

        if (arrayString.startsWith("[") && arrayString.endsWith("]")) {
            arrayString = arrayString.substring(1, arrayString.length()-1);
        }

        return Arrays.stream(StringUtils.commaDelimitedListToStringArray(arrayString))
                .map(VariableUtils::cutOffDoubleQuotes)
                .map(String::trim)
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
     * Numeric value comparable automatically converts types to numeric values for
     * comparison.
     */
    private class NumericComparable implements Comparable {

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
}
