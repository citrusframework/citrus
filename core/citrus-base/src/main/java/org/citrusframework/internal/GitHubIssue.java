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

package org.citrusframework.internal;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Annotation to reference a GitHub issue number associated with a test class or method.
 * It serves as a linking mechanism between reproducers (tests proving certain behavior) and their corresponding GitHub issues.
 * <p>
 * Example usage:
 * <pre>
 * {@code @GitHubIssue(1234)
 * public class MyTest {
 *     // Class implementation
 * }
 *
 * {@code @GitHubIssue(5678)
 * public void testMethod() {
 *     // Method implementation
 * }}
 * </pre>
 */
@Retention(SOURCE)
@Target({METHOD, TYPE})
public @interface GitHubIssue {

    /**
     * The GitHub issue number to reference.
     *
     * @return the issue number in <a href="https://github.com/citrusframework/citrus/issues">the GitHub repository</a>
     */
    int value();
}
