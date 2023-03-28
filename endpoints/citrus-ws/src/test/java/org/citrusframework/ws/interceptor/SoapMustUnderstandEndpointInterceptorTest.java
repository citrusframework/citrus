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

package org.citrusframework.ws.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.xml.namespace.QNameUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SoapMustUnderstandEndpointInterceptorTest {

    @Test
    public void testSingleMustUnderstandHeader() {
        SoapMustUnderstandEndpointInterceptor interceptor = new SoapMustUnderstandEndpointInterceptor();

        interceptor.setAcceptedHeaders(Collections.singletonList("{http://citrusframework.org/soap-mustunderstand}UserId"));

        SoapHeaderElement header = createHeaderMock("{http://citrusframework.org/soap-mustunderstand}UserId");
        Assert.assertTrue(interceptor.understands(header));

    }

    @Test
    public void testSingleMustUnderstandHeaderNegativeWrongLocalPart() {
        SoapMustUnderstandEndpointInterceptor interceptor = new SoapMustUnderstandEndpointInterceptor();

        interceptor.setAcceptedHeaders(Collections.singletonList("{http://citrusframework.org/soap-mustunderstand}UserId"));

        SoapHeaderElement header = createHeaderMock("{http://citrusframework.org/soap-mustunderstand}WrongId");
        Assert.assertFalse(interceptor.understands(header));

    }

    @Test
    public void testSingleMustUnderstandHeaderNegativeWrongNamespace() {
        SoapMustUnderstandEndpointInterceptor interceptor = new SoapMustUnderstandEndpointInterceptor();

        interceptor.setAcceptedHeaders(Collections.singletonList("{http://citrusframework.org/soap-mustunderstand}UserId"));

        SoapHeaderElement header = createHeaderMock("{http://citrusframework.org/soap-wrong}UserId");
        Assert.assertFalse(interceptor.understands(header));

    }

    @Test
    public void testMustUnderstandHeaderDefaultNamespace() {
        SoapMustUnderstandEndpointInterceptor interceptor = new SoapMustUnderstandEndpointInterceptor();

        interceptor.setAcceptedHeaders(Collections.singletonList("UserId"));

        SoapHeaderElement header = createHeaderMock("UserId");
        Assert.assertTrue(interceptor.understands(header));

    }

    @Test
    public void testMultipleMustUnderstandHeaders() {
        SoapMustUnderstandEndpointInterceptor interceptor = new SoapMustUnderstandEndpointInterceptor();

        List<String> headers = new ArrayList<String>();
        headers.add("{http://citrusframework.org/soap-mustunderstand}UserId");
        headers.add("TransactionId");
        headers.add("{http://citrusframework.org/soap-mustunderstand/operation}Operation");
        headers.add("{http://citrusframework.org/tracking/soap-mustunderstand}TrackingId");
        interceptor.setAcceptedHeaders(headers);

        Assert.assertTrue(interceptor.understands(createHeaderMock("{http://citrusframework.org/soap-mustunderstand}UserId")));
        Assert.assertTrue(interceptor.understands(createHeaderMock("{http://citrusframework.org/soap-mustunderstand/operation}Operation")));
        Assert.assertFalse(interceptor.understands(createHeaderMock("{http://citrusframework.org/soap-mustunderstand}TrackingId")));
        Assert.assertTrue(interceptor.understands(createHeaderMock("TransactionId")));
    }

    /**
     * Construct a mocked soap header element.
     *
     * @return mocked soap header.
     */
    private SoapHeaderElement createHeaderMock(String qNameString) {
        SoapHeaderElement header = Mockito.mock(SoapHeaderElement.class);

        reset(header);
        when(header.getName()).thenReturn(QNameUtils.parseQNameString(qNameString));

        return header;
    }
}
