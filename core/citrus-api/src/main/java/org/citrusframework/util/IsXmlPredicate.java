package org.citrusframework.util;

import java.util.function.Predicate;

/**
 * Tests if a string represents a XML. An empty string is considered to be a valid XML.
 */
public class IsXmlPredicate implements Predicate<String> {
    
    private static final IsXmlPredicate INSTANCE = new IsXmlPredicate();

    private IsXmlPredicate() {
        // Singleton
    }

    public static IsXmlPredicate getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean test(String toTest) {
        
        if (toTest != null) {
            toTest = toTest.trim();
        }
        return toTest!=null && (toTest.length() == 0 || toTest.startsWith("<"));
    }
}