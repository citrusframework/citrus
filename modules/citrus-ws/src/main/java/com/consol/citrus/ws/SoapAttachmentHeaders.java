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
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 */
public abstract class SoapAttachmentHeaders {
    public static final String PREFIX = "citrus_soapattachment_";
    
    public static final String CONTENT_ID = PREFIX + "contentId";
    
    public static final String CONTENT_TYPE = PREFIX + "contentType";
    
    public static final String CONTENT = PREFIX + "content";
    
    public static final String CHARSET_NAME = PREFIX + "charset";
}
