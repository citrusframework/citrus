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

package org.citrusframework.junit.integration;

import org.citrusframework.ShouldFailGroup;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.junit.spring.JUnit4CitrusSpringSupport;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class FailJUnit4RuntimeExceptionIT extends JUnit4CitrusSpringSupport {

    @Test(expected = TestCaseFailedException.class)
    @Category( ShouldFailGroup.class )
    @CitrusTest
    public void failTest() {
        throw new RuntimeException("This test should fail because of runtime exception");
    }
}
