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

package org.citrusframework.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Derives a human-readable report name from the type of a test behavior.
 * <p>
 * A named class is de-camel-cased into lower case words, keeping acronyms as they are
 * ({@code SendSOAPRequest} becomes {@code send SOAP request}). Nested classes and records use their
 * simple name only. Lambdas and anonymous classes have no meaningful name, so they get the generic
 * name {@value #GENERIC_NAME} rather than an invented one.
 */
public final class BehaviorNames {

    /** Name used for behaviors whose type does not describe them, such as lambdas and anonymous classes */
    public static final String GENERIC_NAME = "behavior";

    private static final String LAMBDA_MARKER = "$$Lambda";

    private BehaviorNames() {
        //prevent instantiation of utility class
    }

    /**
     * Derives the report name from given behavior type.
     * @param type the behavior type, may be null.
     * @return the de-camel-cased simple name of the type or the generic name.
     */
    public static String of(Class<?> type) {
        if (type == null || isUnnamed(type)) {
            return GENERIC_NAME;
        }

        String simpleName = stripGeneratedSuffix(type.getSimpleName());
        if (simpleName.isEmpty()) {
            return GENERIC_NAME;
        }

        return humanize(simpleName);
    }

    private static boolean isUnnamed(Class<?> type) {
        return type.isSynthetic() || type.isAnonymousClass() || type.getName().contains(LAMBDA_MARKER);
    }

    /**
     * Proxies and mocks append generated segments after a '$' to the original class name.
     */
    private static String stripGeneratedSuffix(String simpleName) {
        int generated = simpleName.indexOf('$');
        return generated < 0 ? simpleName : simpleName.substring(0, generated);
    }

    private static String humanize(String simpleName) {
        List<String> words = new ArrayList<>();
        StringBuilder word = new StringBuilder();

        for (int i = 0; i < simpleName.length(); i++) {
            char current = simpleName.charAt(i);

            if (current == '_') {
                addWord(words, word);
                continue;
            }

            if (Character.isUpperCase(current) && !word.isEmpty() && startsNewWord(simpleName, i)) {
                addWord(words, word);
            }

            word.append(current);
        }

        addWord(words, word);

        return String.join(" ", words);
    }

    /**
     * An upper case character starts a new word unless it continues an acronym. The last capital of an
     * acronym followed by a lower case character starts the next word ("SOAPRequest" is "SOAP", "Request").
     */
    private static boolean startsNewWord(String simpleName, int index) {
        boolean previousIsUpperCase = Character.isUpperCase(simpleName.charAt(index - 1));
        boolean nextIsLowerCase = index + 1 < simpleName.length() && Character.isLowerCase(simpleName.charAt(index + 1));
        return !previousIsUpperCase || nextIsLowerCase;
    }

    private static void addWord(List<String> words, StringBuilder word) {
        if (word.isEmpty()) {
            return;
        }

        String value = word.toString();
        words.add(isAcronym(value) ? value : value.toLowerCase(Locale.ROOT));
        word.setLength(0);
    }

    private static boolean isAcronym(String word) {
        return word.length() > 1 && word.chars().noneMatch(Character::isLowerCase);
    }
}
