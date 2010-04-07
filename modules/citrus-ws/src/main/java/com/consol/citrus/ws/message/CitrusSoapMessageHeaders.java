/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
    
    /** Soap header content data */
    public static final String SOAP_HEADER_CONTENT = SOAP_PREFIX + "header_content";
    
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
