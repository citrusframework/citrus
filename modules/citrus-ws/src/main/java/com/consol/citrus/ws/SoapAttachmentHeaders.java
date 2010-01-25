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

package com.consol.citrus.ws;


/**
 * Special SOAP attachment header names used as internal message headers.
 * 
 * @author Christoph Deppisch
 */
public abstract class SoapAttachmentHeaders {
    /** Citrus specific header prefix */
    public static final String PREFIX = "citrus_soapattachment_";
    
    /** Content id header name*/
    public static final String CONTENT_ID = PREFIX + "contentId";
    
    /** Content type header name*/
    public static final String CONTENT_TYPE = PREFIX + "contentType";
    
    /** Content body header name*/
    public static final String CONTENT = PREFIX + "content";
    
    /** Charset header name*/
    public static final String CHARSET_NAME = PREFIX + "charset";
}
