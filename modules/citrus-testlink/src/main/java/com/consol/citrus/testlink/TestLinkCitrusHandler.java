/*
 * File: TestLinkCitrusHandler.java
 *
 * Copyright (c) 2006-2012 the original author or authors.
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
 *
 * last modified: Saturday, January 21, 2012 (12:05) by: Matthias Beil
 */
package com.consol.citrus.testlink;

import java.util.List;

/**
 * Handler handles interaction with TestLink.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public interface TestLinkCitrusHandler {

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * Read all test case(s) from TestLink and returns them as a list.
     *
     * @param url
     *            TestLink URL.
     * @param key
     *            Development key needed for authorization.
     *
     * @return List of TestLink CITRUS beans. Will never be {@code null}, but might be empty.
     */
    List<TestLinkCitrusBean> readTestCases(final String url, final String key);

}
