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

package org.citrusframework.main.scan;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @since 2.7.4
 */
public abstract class AbstractTestScanner implements TestScanner {

    /** Test name patterns to include */
    private final String[] includes;
    private final Set<Pattern> includePatterns;

    public AbstractTestScanner(String... includes) {
        if (includes.length > 0) {
            this.includes = includes;
        } else {
            this.includes = new String[] { "^.*IT$", "^.*ITCase$", "^IT.*$" };
        }
        includePatterns = Arrays.stream(includes)
            .map(Pattern::compile)
            .collect(Collectors.toSet());
    }

    protected boolean isIncluded(String className) {
        return getIncludePatterns().stream()
                .parallel()
                .anyMatch(pattern -> pattern.matcher(className).matches());
    }

    /**
     * Gets the includes.
     *
     * @return
     */
    public String[] getIncludes() {
        return includes;
    }

    /**
     * Gets the include patterns.
     *
     * @return
     */
    public Set<Pattern> getIncludePatterns() {
        return includePatterns;
    }
}
