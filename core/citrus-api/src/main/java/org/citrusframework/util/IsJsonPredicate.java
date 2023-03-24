package org.citrusframework.util;

import java.util.function.Predicate;

/**
 * Tests if a string represents a Json. An empty string is considered to be a
 * valid Json.
 */
public class IsJsonPredicate implements Predicate<String> {

    private static final IsJsonPredicate INSTANCE = new IsJsonPredicate();

    private IsJsonPredicate() {
        // Singleton
    }

    public static IsJsonPredicate getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean test(String toTest) {

        if (toTest != null) {
            toTest = toTest.trim();
        }

        return toTest != null && (toTest.length() == 0 || toTest.startsWith("{") || toTest.startsWith("["));
    }
}