/*
 * Copyright 2006-2018 the original author or authors.
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

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public abstract class AbstractTestScanner implements TestScanner {

    /** Test name patterns to include */
    private final String[] includes;

    public AbstractTestScanner(String... includes) {
        if (includes.length > 0) {
            this.includes = includes;
        } else {
            this.includes = new String[] { "^.*IT$", "^.*ITCase$", "^IT.*$" };
        }
    }

    protected boolean isIncluded(String className) {
        return Stream.of(getIncludes())
                .parallel()
                .map(Pattern::compile)
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
}
