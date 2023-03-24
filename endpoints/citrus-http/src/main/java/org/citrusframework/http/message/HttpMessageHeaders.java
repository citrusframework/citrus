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

package org.citrusframework.http.message;

import org.citrusframework.message.MessageHeaders;

/**
 * @author Christoph Deppisch
 */
public abstract class HttpMessageHeaders {
    
    /**
     * Prevent instantiation.
     */
    private HttpMessageHeaders() {
    }
    
    /** Special header prefix for http transport headers in SOAP message sender */
    public static final String HTTP_PREFIX = MessageHeaders.PREFIX + "http_";
    
    public static final String HTTP_STATUS_CODE = HTTP_PREFIX + "status_code";

    public static final String HTTP_VERSION = HTTP_PREFIX + "version";
    
    public static final String HTTP_REASON_PHRASE = HTTP_PREFIX + "reason_phrase";
    
    public static final String HTTP_REQUEST_METHOD = HTTP_PREFIX + "method";
    
    public static final String HTTP_CONTEXT_PATH = HTTP_PREFIX + "context_path";
    
    public static final String HTTP_REQUEST_URI = HTTP_PREFIX + "request_uri";
    
    public static final String HTTP_QUERY_PARAMS = HTTP_PREFIX + "query_params";

    public static final String HTTP_COOKIE_PREFIX = HTTP_PREFIX + "cookie_";

    /** Http content type */
    public static final String HTTP_CONTENT_TYPE = "Content-Type";

    /** Http accept */
    public static final String HTTP_ACCEPT = "Accept";
}
