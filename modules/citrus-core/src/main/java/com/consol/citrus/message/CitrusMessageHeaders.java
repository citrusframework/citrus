/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.message;

/**
 * Citrus specific message headers.
 * 
 * @author Christoph Deppisch
 */
public class CitrusMessageHeaders {
    /**
     * Prevent instantiation.
     */
    private CitrusMessageHeaders() {}
    
    /** Common header name prefix */
    public static final String PREFIX = "citrus_";
    
    /** Synchronous message correlation */
    public static final String SYNC_MESSAGE_CORRELATOR = PREFIX + "sync_message_correlator";
    
    /** Header content data */
    public static final String HEADER_CONTENT = PREFIX + "header_content";
}
