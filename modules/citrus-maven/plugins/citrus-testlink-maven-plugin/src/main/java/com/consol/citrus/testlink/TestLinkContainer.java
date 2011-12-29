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
 * File: TestLinkContainer.java
 * last modified: Thursday, December 29, 2011 (15:50) by: Matthias Beil
 */
package com.consol.citrus.testlink;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Defines interface to all objects related to interface with TestLink.
 * 
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public interface TestLinkContainer {

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * Establish connection to TestLink. A new connection object is created and held in this container. So this method
     * must be called before all others.
     * 
     * @param url
     *            URL to TestLink. The path to the XML RPC will be added in this method.
     * @param devKey
     *            Development key as generated in TestLink.
     * 
     * @throws MojoExecutionException
     *             Thrown in case the connection could not be established.
     */
    void connect(final String url, final String devKey) throws MojoExecutionException;

}
