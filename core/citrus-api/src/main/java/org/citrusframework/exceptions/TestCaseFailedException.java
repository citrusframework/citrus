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

package org.citrusframework.exceptions;

import java.io.Serial;

/**
 * Base exception marking failure of test case. Used to force failure of TestNG and JUnit
 * test case.
 *
 */
public class TestCaseFailedException extends CitrusRuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public TestCaseFailedException(Throwable cause) {
        super(cause.getMessage() != null &&  !cause.getMessage().isBlank() ? cause.getMessage() : "Test case failed", cause);
    }
}
