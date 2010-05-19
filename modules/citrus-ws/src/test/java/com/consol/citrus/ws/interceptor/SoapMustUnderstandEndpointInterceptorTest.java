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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.*;

import org.easymock.EasyMock;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.xml.namespace.QNameUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class SoapMustUnderstandEndpointInterceptorTest {

    @Test
    public void testSingleMustUnderstandHeader() {
        SoapMustUnderstandEndpointInterceptor interceptor = new SoapMustUnderstandEndpointInterceptor();
        
        interceptor.setAcceptedHeaders(Collections.singletonList("{http://www.consol.com/soap-mustunderstand}UserId"));
        
        SoapHeaderElement header = createHeaderMock("{http://www.consol.com/soap-mustunderstand}UserId");
        Assert.assertTrue(interceptor.understands(header));
        
        verify(header);
    }
    
    @Test
    public void testSingleMustUnderstandHeaderNegativeWrongLocalPart() {
        SoapMustUnderstandEndpointInterceptor interceptor = new SoapMustUnderstandEndpointInterceptor();
        
        interceptor.setAcceptedHeaders(Collections.singletonList("{http://www.consol.com/soap-mustunderstand}UserId"));
        
        SoapHeaderElement header = createHeaderMock("{http://www.consol.com/soap-mustunderstand}WrongId");
        Assert.assertFalse(interceptor.understands(header));
        
        verify(header);
    }
    
    @Test
    public void testSingleMustUnderstandHeaderNegativeWrongNamespace() {
        SoapMustUnderstandEndpointInterceptor interceptor = new SoapMustUnderstandEndpointInterceptor();
        
        interceptor.setAcceptedHeaders(Collections.singletonList("{http://www.consol.com/soap-mustunderstand}UserId"));
        
        SoapHeaderElement header = createHeaderMock("{http://www.consol.com/soap-wrong}UserId");
        Assert.assertFalse(interceptor.understands(header));
        
        verify(header);
    }
    
    @Test
    public void testMustUnderstandHeaderDefaultNamespace() {
        SoapMustUnderstandEndpointInterceptor interceptor = new SoapMustUnderstandEndpointInterceptor();
        
        interceptor.setAcceptedHeaders(Collections.singletonList("UserId"));
        
        SoapHeaderElement header = createHeaderMock("UserId");
        Assert.assertTrue(interceptor.understands(header));
        
        verify(header);
    }
    
    @Test
    public void testMultipleMustUnderstandHeaders() {
        SoapMustUnderstandEndpointInterceptor interceptor = new SoapMustUnderstandEndpointInterceptor();
        
        List<String> headers = new ArrayList<String>();
        headers.add("{http://www.consol.com/soap-mustunderstand}UserId");
        headers.add("TransactionId");
        headers.add("{http://www.consol.com/soap-mustunderstand/operation}Operation");
        headers.add("{http://www.consol.com/tracking/soap-mustunderstand}TrackingId");
        interceptor.setAcceptedHeaders(headers);
        
        Assert.assertTrue(interceptor.understands(createHeaderMock("{http://www.consol.com/soap-mustunderstand}UserId")));
        Assert.assertTrue(interceptor.understands(createHeaderMock("{http://www.consol.com/soap-mustunderstand/operation}Operation")));
        Assert.assertFalse(interceptor.understands(createHeaderMock("{http://www.consol.com/soap-mustunderstand}TrackingId")));
        Assert.assertTrue(interceptor.understands(createHeaderMock("TransactionId")));
    }

    /**
     * Construct a mocked soap header element.
     * 
     * @return mocked soap header.
     */
    private SoapHeaderElement createHeaderMock(String qNameString) {
        SoapHeaderElement header = EasyMock.createMock(SoapHeaderElement.class);

        reset(header);
        expect(header.getName()).andReturn(QNameUtils.parseQNameString(qNameString)).anyTimes();
        replay(header);
        
        return header; 
    }
}
