/*
 * File: CitrusTestLinkHandler.java
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
 * last modified: Saturday, January 21, 2012 (11:49) by: Matthias Beil
 */
package com.consol.citrus.testlink;

/**
 * Interface defining methods for handling CITRUS to TestLink functionality.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public interface CitrusTestLinkHandler {

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * Write CITRUS TestLink bean to TestLink. Returning in the bean the result of writing to
     * TestLink.
     *
     * @param bean
     *            CITRUS TestLink bean holding all values to write to TestLink.
     */
    void writeToTestLink(final CitrusTestLinkBean bean);

}
