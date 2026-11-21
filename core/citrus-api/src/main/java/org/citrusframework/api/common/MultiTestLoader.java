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

package org.citrusframework.api.common;

import java.util.Optional;

/**
 * Optional test loader capability: the test source may hold several test cases that share file-level
 * declarations and the hooks beforeAll, beforeEach, afterEach and afterAll.
 * <p>
 * Host runners that want to report each test case of such a source on its own (e.g. as children of a
 * JUnit Jupiter dynamic container) open the source with {@link #openMultiTest()} and drive the returned
 * {@link MultiTestRun}. All other hosts simply call {@link TestLoader#load()}, which runs all test cases
 * of the source as part of one host test.
 *
 * @since 5.3
 */
public interface MultiTestLoader {

    /**
     * Reads and validates the test source without running anything. Returns the run when the source is a
     * multi-test source, and an empty optional for a single-test source. The parse result of a single-test
     * source is kept, so the next {@link TestLoader#load()} does not read the source again.
     * @return the multi-test run, or empty for a single-test source.
     * @throws org.citrusframework.exceptions.CitrusRuntimeException when the source cannot be read or
     * parsed, or is not a valid multi-test source.
     */
    Optional<MultiTestRun> openMultiTest();
}
