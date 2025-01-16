package org.citrusframework.functions.core;

import org.apache.commons.lang3.StringUtils;
import org.citrusframework.context.TestContext;
import org.citrusframework.functions.Function;

import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJson;
/**
 * This function takes a JSON string as input and escapes all double quotes within it.
 *
 * <p>The input must be a single non-empty string containing a valid JSON, wrapped in double quotes.</p>
 * <p>If the input is invalid (null, empty, or contains more than one string), an {@link IllegalArgumentException} will be thrown.</p>
 *
 * <p>Example input: <code>"{\"mySuperJson\": \"valium\"}"</code></p>
 * <p>Example output: <code>"{\\\"mySuperJson\\\": \\\"valium\\\"}"</code></p>
 *
 */
public class EscapeJsonFunction implements Function {
    @Override
    public String execute(List<String> list, TestContext testContext) {
        if (Objects.isNull(list) || list.size() != 1 || StringUtils.isEmpty(list.get(0)) || list.get(0).isEmpty()) {
            throw new IllegalArgumentException(
                    "Parameter List is input is invalid (null, empty, or contains more than one string). The input must be a single non-empty string containing a valid JSON, wrapped in double quotes to be transformed into a valid string."
            );
        }
        return escapeJson(list.get(0));
    }
}
