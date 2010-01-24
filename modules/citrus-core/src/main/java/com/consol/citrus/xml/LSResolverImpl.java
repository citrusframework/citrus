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

package com.consol.citrus.xml;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Very basic LSResolver implementation for resolving dtd resources by their systemId.
 * 
 * @author Christoph Deppisch
 */
public class LSResolverImpl implements LSResourceResolver {
    /** DOM implementation */
    private DOMImplementationLS domImpl;
    
    /**
     * Constructor
     * @param domImpl
     */
    public LSResolverImpl(DOMImplementationLS domImpl) {
        this.domImpl = domImpl;
    }
    
    /**
     * @see org.w3c.dom.ls.LSResourceResolver#resolveResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public LSInput resolveResource(String type, String namespaceURI,
            String publicId, String systemId, String baseURI) {
        LSInput input = domImpl.createLSInput();
        try {
            input.setByteStream(new ClassPathResource(systemId).getInputStream());
        } catch (IOException e) {
            return null;
        }
        
        return input;
    }
}
