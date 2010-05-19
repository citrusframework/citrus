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

package com.consol.citrus.ws.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.server.SoapEndpointInterceptor;

/**
 * Handles and accepts must understand header entries in SOAP requests. The supported header entries are configured
 * via simple HashMap where key is the namespace uri and value the header name (local part).
 * 
 * @author Christoph Deppisch
 */
public class SoapMustUnderstandEndpointInterceptor implements SoapEndpointInterceptor {

    private List<String> acceptedHeaders = new ArrayList<String>();
    
    /**
     * (non-Javadoc)
     * @see org.springframework.ws.soap.server.SoapEndpointInterceptor#understands(org.springframework.ws.soap.SoapHeaderElement)
     */
    public boolean understands(SoapHeaderElement header) {
        //see if header is accepted
        if(header.getName() != null && acceptedHeaders.contains(header.getName().toString())) {
            return true;
        }
        
        return false;
    }
    
    /**
     * (non-Javadoc)
     * @see org.springframework.ws.server.EndpointInterceptor#handleFault(org.springframework.ws.context.MessageContext, java.lang.Object)
     */
    public boolean handleFault(MessageContext messageContext, Object endpoint)
            throws Exception {
        return true;
    }

    /**
     * (non-Javadoc)
     * @see org.springframework.ws.server.EndpointInterceptor#handleRequest(org.springframework.ws.context.MessageContext, java.lang.Object)
     */
    public boolean handleRequest(MessageContext messageContext, Object endpoint)
            throws Exception {
        return true;
    }

    /**
     * (non-Javadoc)
     * @see org.springframework.ws.server.EndpointInterceptor#handleResponse(org.springframework.ws.context.MessageContext, java.lang.Object)
     */
    public boolean handleResponse(MessageContext messageContext, Object endpoint)
            throws Exception {
        return true;
    }

    /**
     * @param acceptedHeaders the acceptedHeaders to set
     */
    public void setAcceptedHeaders(List<String> acceptedHeaders) {
        this.acceptedHeaders = acceptedHeaders;
    }
}
