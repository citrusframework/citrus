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

package org.citrusframework.validation.assertj.matcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.assertj.core.api.Assert;
import org.assertj.core.api.Assertions;
import org.citrusframework.exceptions.ValidationException;

/**
 * Applies a parsed {@code @assertj(...)@} chain to {@code assertThat(value)} by reflection.
 * <p>
 * Only public methods declared by AssertJ that return an AssertJ {@link Assert} (or end the chain with {@code void})
 * can be called, and arguments are literals, so a chain cannot reach anything outside AssertJ's assertions. Among overloads that accept the literals,
 * the one needing the fewest conversions wins; a remaining tie is reported instead of guessed.
 */
final class AssertionChainInvoker {

    private static final String AS_LIST = "asList";
    private static final String AS_MAP = "asMap";

    private static final int NO_MATCH = -1;

    private static final Map<CandidateKey, List<Method>> CANDIDATES = new ConcurrentHashMap<>();

    private AssertionChainInvoker() {
        // prevent instantiation of utility class
    }

    /**
     * Runs the chain on the value.
     * @throws AssertionError when an assertion in the chain fails
     * @throws ValidationException when a call cannot be resolved to an AssertJ assertion
     */
    static void invoke(String value, List<AssertionCall> calls) {
        Object assertion = start(value, calls.get(0));
        int first = isCollectionConversion(calls.get(0)) ? 1 : 0;

        AssertionCall previous = calls.get(0);
        for (AssertionCall call : calls.subList(first, calls.size())) {
            if (isCollectionConversion(call)) {
                throw new ValidationException(String.format("'%s' is only supported as the first call of an @assertj()@ expression", call.name()));
            }
            if (assertion == null) {
                throw new ValidationException(String.format("'%s' ends the assertion chain and cannot be followed by '%s'", previous.name(), call.name()));
            }
            assertion = call(assertion, call);
            previous = call;
        }
    }

    private static Object start(String value, AssertionCall first) {
        if (isCollectionConversion(first)) {
            return first.name().equals(AS_LIST)
                    ? Assertions.assertThat(CollectionValueParser.toList(value))
                    : Assertions.assertThat(CollectionValueParser.toMap(value));
        }

        return Assertions.assertThat(value);
    }

    private static boolean isCollectionConversion(AssertionCall call) {
        return call.arguments().isEmpty() && (call.name().equals(AS_LIST) || call.name().equals(AS_MAP));
    }

    /**
     * Calls one assertion and returns the assertion to continue with, or {@code null} after a {@code void} assertion.
     */
    private static Object call(Object assertion, AssertionCall call) {
        Method method = resolve(assertion.getClass(), call);
        try {
            if (!Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
                method.setAccessible(true);
            }
            Object result = method.invoke(assertion, arguments(method, call.arguments()));
            if (method.getReturnType() != void.class && !(result instanceof Assert<?, ?>)) {
                throw notAnAssertion(call, assertion.getClass());
            }
            return result;
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof AssertionError assertionError) {
                throw assertionError;
            }
            throw new AssertionError(e.getCause().getMessage(), e.getCause());
        } catch (IllegalAccessException e) {
            throw new ValidationException(String.format("Failed to call AssertJ assertion '%s' on %s", call.name(), assertion.getClass().getSimpleName()), e);
        }
    }

    private static Method resolve(Class<?> assertType, AssertionCall call) {
        List<Method> candidates = CANDIDATES.computeIfAbsent(new CandidateKey(assertType, call.name(), call.arguments().size()),
                key -> candidates(key.type(), key.name(), key.arity()));

        List<Method> assertions = candidates.stream()
                .filter(AssertionChainInvoker::isAssertionMethod)
                .toList();
        if (!candidates.isEmpty() && assertions.isEmpty()) {
            throw notAnAssertion(call, assertType);
        }

        List<ScoredMethod> applicable = new ArrayList<>();
        for (Method method : assertions) {
            int score = score(method, call.arguments());
            if (score != NO_MATCH) {
                applicable.add(new ScoredMethod(method, score));
            }
        }

        if (applicable.isEmpty()) {
            throw new ValidationException(String.format("No AssertJ assertion '%s(%s)' on %s",
                    call.name(), argumentTypes(call.arguments()), assertType.getSimpleName()));
        }

        int best = applicable.stream().mapToInt(ScoredMethod::score).min().getAsInt();
        List<Method> bestMethods = applicable.stream()
                .filter(scored -> scored.score() == best)
                .map(ScoredMethod::method)
                .toList();

        return mostSpecific(bestMethods, call, assertType);
    }

    /**
     * An assertion method is declared by AssertJ and returns an assertion, {@code void}, or a generic self type
     * (such as {@code as(...)}); the generic case is confirmed on the returned value.
     */
    private static boolean isAssertionMethod(Method method) {
        Class<?> returnType = method.getReturnType();
        return method.getDeclaringClass().getName().startsWith("org.assertj.")
                && (returnType == void.class
                    || Assert.class.isAssignableFrom(returnType)
                    || method.getGenericReturnType() instanceof TypeVariable<?>);
    }

    private static ValidationException notAnAssertion(AssertionCall call, Class<?> assertType) {
        return new ValidationException(String.format("'%s' is not an AssertJ assertion method on %s", call.name(), assertType.getSimpleName()));
    }

    private static List<Method> candidates(Class<?> type, String name, int arity) {
        return Arrays.stream(type.getMethods())
                .filter(method -> method.getName().equals(name))
                .filter(method -> !method.isBridge() && !method.isSynthetic())
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.getParameterCount() == arity
                        || (method.isVarArgs() && arity >= method.getParameterCount() - 1))
                .toList();
    }

    /**
     * Picks the single most specific method among equally scored candidates; a primitive parameter is preferred
     * over its wrapper. More than one remaining candidate is an ambiguity error.
     */
    private static Method mostSpecific(List<Method> methods, AssertionCall call, Class<?> assertType) {
        if (methods.size() == 1) {
            return methods.get(0);
        }

        List<Method> specific = methods.stream()
                .filter(method -> methods.stream().noneMatch(other -> other != method && isStrictlyMoreSpecific(other, method)))
                .sorted(Comparator.comparingLong(AssertionChainInvoker::wrapperParameterCount))
                .toList();

        Method first = specific.get(0);
        boolean ambiguous = specific.stream()
                .skip(1)
                .anyMatch(other -> !sameBoxedSignature(first, other) || wrapperParameterCount(other) == wrapperParameterCount(first));
        if (ambiguous) {
            throw new ValidationException(String.format("Ambiguous AssertJ assertion '%s(%s)' on %s - candidates: %s",
                    call.name(), argumentTypes(call.arguments()), assertType.getSimpleName(),
                    specific.stream().map(AssertionChainInvoker::signature).collect(Collectors.joining(", "))));
        }

        return first;
    }

    private static boolean isStrictlyMoreSpecific(Method method, Method other) {
        return isAtLeastAsSpecific(method, other) && !isAtLeastAsSpecific(other, method);
    }

    private static boolean isAtLeastAsSpecific(Method method, Method other) {
        Class<?>[] parameters = method.getParameterTypes();
        Class<?>[] otherParameters = other.getParameterTypes();
        if (parameters.length != otherParameters.length) {
            return false;
        }

        for (int i = 0; i < parameters.length; i++) {
            if (!wrap(otherParameters[i]).isAssignableFrom(wrap(parameters[i]))) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameBoxedSignature(Method method, Method other) {
        return isAtLeastAsSpecific(method, other) && isAtLeastAsSpecific(other, method);
    }

    private static long wrapperParameterCount(Method method) {
        return Arrays.stream(method.getParameterTypes()).filter(type -> !type.isPrimitive()).count();
    }

    /**
     * Total conversion cost of passing the literals to the method, or {@link #NO_MATCH} when a literal does not fit.
     */
    private static int score(Method method, List<Object> arguments) {
        Class<?>[] parameters = method.getParameterTypes();
        int fixed = method.isVarArgs() ? parameters.length - 1 : parameters.length;

        int total = 0;
        for (int i = 0; i < fixed; i++) {
            int score = score(parameters[i], arguments.get(i));
            if (score == NO_MATCH) {
                return NO_MATCH;
            }
            total += score;
        }

        if (method.isVarArgs()) {
            Class<?> component = parameters[fixed].getComponentType();
            for (int i = fixed; i < arguments.size(); i++) {
                int score = score(component, arguments.get(i));
                if (score == NO_MATCH) {
                    return NO_MATCH;
                }
                total += score;
            }
            total += 1;
        }

        return total;
    }

    /**
     * Cost of passing one literal: 0 for an exact type, 1 for a conversion, 2 for {@code Object}.
     */
    private static int score(Class<?> parameter, Object argument) {
        if (argument == null) {
            return parameter.isPrimitive() ? NO_MATCH : 2;
        }

        Class<?> target = wrap(parameter);
        if (target.equals(argument.getClass())) {
            return 0;
        }

        if (target.equals(Object.class)) {
            return 2;
        }

        if (target.isInstance(argument) || convert(argument, target) != null) {
            return 1;
        }

        return NO_MATCH;
    }

    private static Object[] arguments(Method method, List<Object> arguments) {
        Class<?>[] parameters = method.getParameterTypes();
        int fixed = method.isVarArgs() ? parameters.length - 1 : parameters.length;

        Object[] values = new Object[parameters.length];
        for (int i = 0; i < fixed; i++) {
            values[i] = coerce(arguments.get(i), parameters[i]);
        }

        if (method.isVarArgs()) {
            Class<?> component = parameters[fixed].getComponentType();
            Object varargs = java.lang.reflect.Array.newInstance(component, arguments.size() - fixed);
            for (int i = fixed; i < arguments.size(); i++) {
                java.lang.reflect.Array.set(varargs, i - fixed, coerce(arguments.get(i), component));
            }
            values[fixed] = varargs;
        }

        return values;
    }

    private static Object coerce(Object argument, Class<?> parameter) {
        if (argument == null) {
            return null;
        }

        Class<?> target = wrap(parameter);
        if (target.isInstance(argument)) {
            return argument;
        }

        return convert(argument, target);
    }

    /**
     * Converts a numeric or text literal to a numeric or character type, or returns {@code null} when it does not fit.
     */
    private static Object convert(Object argument, Class<?> target) {
        if (argument instanceof String text) {
            return target.equals(Character.class) && text.length() == 1 ? text.charAt(0) : null;
        }

        if (!(argument instanceof Number number)) {
            return null;
        }

        boolean integral = argument instanceof Integer || argument instanceof Long;
        if (target.equals(Long.class) && integral) {
            return number.longValue();
        } else if (target.equals(Integer.class) && integral && number.longValue() == number.intValue()) {
            return number.intValue();
        } else if (target.equals(Short.class) && integral && number.longValue() == number.shortValue()) {
            return number.shortValue();
        } else if (target.equals(Byte.class) && integral && number.longValue() == number.byteValue()) {
            return number.byteValue();
        } else if (target.equals(Double.class)) {
            return number.doubleValue();
        } else if (target.equals(Float.class)) {
            return number.floatValue();
        }

        return null;
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }

        return switch (type.getName()) {
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "double" -> Double.class;
            case "float" -> Float.class;
            case "short" -> Short.class;
            case "byte" -> Byte.class;
            case "char" -> Character.class;
            case "boolean" -> Boolean.class;
            default -> Void.class;
        };
    }

    private static String argumentTypes(List<Object> arguments) {
        return arguments.stream()
                .map(argument -> argument == null ? "null" : argument.getClass().getSimpleName())
                .collect(Collectors.joining(", "));
    }

    private static String signature(Method method) {
        return method.getName() + Arrays.stream(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", ", "(", ")"));
    }

    private record CandidateKey(Class<?> type, String name, int arity) {
    }

    private record ScoredMethod(Method method, int score) {
    }
}
