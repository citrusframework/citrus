/*
 * File: CitrusTestLinkFactory.java
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
 * last modified: Saturday, January 14, 2012 (12:18) by: Matthias Beil
 */
package com.consol.citrus.testlink.citrus;

/**
 * DOCUMENT ME!
 * 
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public abstract class CitrusTestLinkFactory {

    // ~ Constructors --------------------------------------------------------------------------------------------------

    /**
     * Constructor for {@code CitrusTestLinkFactory} class.
     */
    private CitrusTestLinkFactory() {

        super();
    }

    // ~ Methods -------------------------------------------------------------------------------------------------------

    /**
     * Returns the value of the {@code handler} field.
     * 
     * @return {@code handler} field.
     */
    public static final CitrusTestLinkHandler getHandler() {

        return new CitrusTestlinkHandlerImpl();
    }

}
