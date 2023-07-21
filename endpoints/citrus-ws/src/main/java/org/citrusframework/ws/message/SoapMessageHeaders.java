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

package org.citrusframework.ws.message;

import org.citrusframework.message.MessageHeaders;

/**
 * @author Christoph Deppisch
 */
public abstract class SoapMessageHeaders {

    /**
     * Prevent instantiation.
     */
    private SoapMessageHeaders() {
    }

    /** Citrus ws specific header prefix */
    public static final String SOAP_PREFIX = MessageHeaders.PREFIX + "soap_";

    /** Special header prefix for http transport headers in SOAP message sender */
    public static final String HTTP_PREFIX = MessageHeaders.PREFIX + "http_";

    /** Special status code header */
    public static final String HTTP_STATUS_CODE = HTTP_PREFIX + "status_code";

    /** Special status reason phrase header */
    public static final String HTTP_REASON_PHRASE = HTTP_PREFIX + "reason_phrase";

    /** Server context path */
    public static final String HTTP_CONTEXT_PATH = HTTP_PREFIX + "context_path";

    /** Full http request uri */
    public static final String HTTP_REQUEST_URI = HTTP_PREFIX + "request_uri";

    /** Http request method */
    public static final String HTTP_REQUEST_METHOD = HTTP_PREFIX + "method";

    /** Http content type */
    public static final String HTTP_CONTENT_TYPE = HTTP_PREFIX + "Content-Type";

    /** Http accept */
    public static final String HTTP_ACCEPT = HTTP_PREFIX + "Accept";

    /** Http query parameters */
    public static final String HTTP_QUERY_PARAMS = HTTP_PREFIX + "query_params";

    /** SOAP action header name */
    public static final String SOAP_ACTION = SOAP_PREFIX + "action";

}
