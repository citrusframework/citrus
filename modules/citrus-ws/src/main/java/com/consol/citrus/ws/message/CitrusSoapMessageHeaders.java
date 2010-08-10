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

package com.consol.citrus.ws.message;

import com.consol.citrus.message.CitrusMessageHeaders;

/**
 * @author Christoph Deppisch
 */
public abstract class CitrusSoapMessageHeaders {
    /** Citrus ws specific header prefix */
    public static final String SOAP_PREFIX = CitrusMessageHeaders.PREFIX + "soap_";

    /** SOAP action header name */
    public static final String SOAP_ACTION = SOAP_PREFIX + "action";
    
    /** Soap fault code specific header */
    public static final String SOAP_FAULT = SOAP_PREFIX + "fault";
    
    /** Soap attachment header prefix */
    public static final String SOAP_ATTACHMENT_PREFIX = SOAP_PREFIX + "attachment_";
    
    /** Content id header name*/
    public static final String CONTENT_ID = SOAP_ATTACHMENT_PREFIX + "contentId";
    
    /** Content type header name*/
    public static final String CONTENT_TYPE = SOAP_ATTACHMENT_PREFIX + "contentType";
    
    /** Content body header name*/
    public static final String CONTENT = SOAP_ATTACHMENT_PREFIX + "content";
    
    /** Charset header name*/
    public static final String CHARSET_NAME = SOAP_ATTACHMENT_PREFIX + "charset";
}
