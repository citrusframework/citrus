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
import com.consol.citrus.validation.matcher.ValidationMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.springframework.util.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
@SuppressWarnings("unchecked")
public class HamcrestValidationMatcher implements ValidationMatcher {

    private List<String> matchers = Arrays.asList( "equalTo", "equalToIgnoringCase", "equalToIgnoringWhiteSpace", "is", "not", "containsString", "startsWith", "endsWith" );

    private List<String> collectionMatchers = Arrays.asList( "hasSize" );

    private List<String> comparableMatchers = Arrays.asList( "greaterThan", "greaterThanOrEqualTo", "lessThan", "lessThanOrEqualTo" );

    private List<String> containerMatchers = Arrays.asList( "is", "not" );

    private List<String> noArgumentMatchers = Arrays.asList( "isEmptyString", "isEmptyOrNullString", "nullValue", "notNullValue", "anything" );

    private List<String> noArgumentCollectionMatchers = Arrays.asList( "empty" );

    private List<String> iterableMatchers = Arrays.asList( "contains", "anyOf", "allOf" );

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

        if (noArgumentCollectionMatchers.contains(matcherName) || collectionMatchers.contains(matcherName)) {
            assertThat(getCollection(matcherValue), getMatcher(matcherName, matcherParameter));
            return;
        } else {
            assertThat(matcherValue, getMatcher(matcherName, matcherParameter));
            return;
        }
    }

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

            if (comparableMatchers.contains(matcherName)) {
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
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new CitrusRuntimeException("Failed to invoke matcher", e);
        }

        throw new CitrusRuntimeException("Unsupported matcher: " + matcherName);
    }

    private Set<String> getCollection(String value) {
        String arrayString = value;

        if (arrayString.startsWith("[") && arrayString.endsWith("]")) {
            arrayString = arrayString.substring(1, arrayString.length()-1);
        }

        return StringUtils.commaDelimitedListToSet(arrayString);
    }


}
