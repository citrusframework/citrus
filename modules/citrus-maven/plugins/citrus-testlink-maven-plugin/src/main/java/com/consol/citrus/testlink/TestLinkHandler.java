/*
 * Copyright 2006-2011 the original author or authors.
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
 * File: TestLinkHandler.java
 * last modified: Friday, December 30, 2011 (12:47) by: Matthias Beil
 */
package com.consol.citrus.testlink;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

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
     * @return List of TestLink beans.
     * 
     * @throws MojoExecutionException
     *             Thrown in case of some error interacting with TestLink.
     */
    List<TestLinkBean> readTestCases() throws MojoExecutionException;

}
