/*
 * File: TestLinkHandler.java
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
 * last modified: Sunday, January 15, 2012 (10:07) by: Matthias Beil
 */
package com.consol.citrus.testlink;

import java.util.List;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPIException;

/**
 * Handler handles interaction with TestLink.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public interface TestLinkHandler {

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * Read all test case(s) from TestLink and returns them as a list.
     *
     * @param url
     *            DOCUMENT ME!
     * @param key
     *            DOCUMENT ME!
     *
     * @return List of TestLink beans.
     *
     * @throws TestLinkAPIException
     *             Thrown in case of some error interacting with TestLink.
     */
    List<TestLinkBean> readTestCases(final String url, final String key) throws TestLinkAPIException;

    /**
     * DOCUMENT ME!
     *
     * @param bean
     *            DOCUMENT ME!
     */
    void writeToTestLink(final TestLinkBean bean);

}
