/*
 *    Copyright 2018 the original author or authors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.consol.citrus.http.servlet;

import org.apache.http.entity.ContentType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class CachingHttpServletRequestWrapperTest {

    private HttpServletRequest serverRequestMock;
    private CachingHttpServletRequestWrapper wrapper;

    @BeforeMethod
    public void setupClasses(){
        serverRequestMock = mock(HttpServletRequest.class);
        wrapper = new CachingHttpServletRequestWrapper(serverRequestMock);
    }

    @DataProvider(name = "queryStringRequestMethods")
    public static Object[] queryStringRequestMethods() {
        return new Object[]{
                RequestMethod.DELETE,
                RequestMethod.GET,
                RequestMethod.HEAD,
                RequestMethod.OPTIONS,
                RequestMethod.PATCH,
                RequestMethod.TRACE
        };
    }

    @DataProvider(name = "bodyPayloadRequestMethods")
    public static Object[] bodyPayloadRequestMethods() {
        return new Object[]{
                RequestMethod.POST,
                RequestMethod.PUT
        };
    }

    @Test
    public void testDelegateGetParameterIfBodyIsNull(){

        //GIVEN
        final String expectedKey = "foobar";
        when(serverRequestMock.getParameterMap()).thenReturn(Collections.singletonMap(expectedKey, new String[]{}));

        //WHEN
        final Map<String, String[]> parameterMap = wrapper.getParameterMap();

        //THEN
        assertEquals(parameterMap.keySet().size(),1);
        assertTrue(parameterMap.containsKey(expectedKey));
    }

    @Test(dataProvider = "queryStringRequestMethods")
    public void testFillMapFromQueryString(final RequestMethod requestMethod) throws Exception{

        //GIVEN
        //Initialize body member
        when(serverRequestMock.getInputStream()).thenReturn(null);
        wrapper.getInputStream();

        when(serverRequestMock.getQueryString()).thenReturn("&" + requestMethod.name() + "=" + requestMethod.name());

        //WHEN
        final Map<String, String[]> parameterMap = wrapper.getParameterMap();

        //THEN
        assertEquals(parameterMap.keySet().size(),1);
        assertTrue(parameterMap.containsKey(requestMethod.name()));
        assertEquals(parameterMap.get(requestMethod.name()), new String[]{requestMethod.name()});
    }

    @Test(dataProvider = "bodyPayloadRequestMethods")
    public void testDelegateGetParameterIfContentTypeNotUrlencoded(final RequestMethod requestMethod)  throws Exception{

        //GIVEN
        //Initialize body member
        when(serverRequestMock.getInputStream()).thenReturn(null);
        wrapper.getInputStream();

        when(serverRequestMock.getContentType()).thenReturn(ContentType.APPLICATION_JSON.toString());

        when(serverRequestMock.getMethod()).thenReturn(requestMethod.name());

        final String expectedKey = "foobar";
        when(serverRequestMock.getParameterMap()).thenReturn(Collections.singletonMap(expectedKey, new String[]{}));

        //WHEN
        final Map<String, String[]> parameterMap = wrapper.getParameterMap();

        //THEN
        assertEquals(parameterMap.keySet().size(),1);
        assertTrue(parameterMap.containsKey(expectedKey));

    }

    @Test(dataProvider = "bodyPayloadRequestMethods")
    public void testParseUrlEncodedBody(final RequestMethod requestMethod)  throws Exception{

        //GIVEN
        //Initialize body member
        when(serverRequestMock.getInputStream())
                .thenReturn(new DelegatingServletInputStream(
                        new ByteArrayInputStream(
                                ("&" + requestMethod.name() + "=" + requestMethod.name()).getBytes())));
        wrapper.getInputStream();

        when(serverRequestMock.getContentType()).thenReturn(ContentType.APPLICATION_FORM_URLENCODED.toString());

        when(serverRequestMock.getMethod()).thenReturn(requestMethod.name());

        //WHEN
        final Map<String, String[]> parameterMap = wrapper.getParameterMap();

        //THEN
        assertEquals(parameterMap.keySet().size(),1);
        assertTrue(parameterMap.containsKey(requestMethod.name()));
        assertEquals(parameterMap.get(requestMethod.name()), new String[]{requestMethod.name()});

    }


    /**
     * Utility class to wrap a byte input stream as a servlet input stream
     */
    class DelegatingServletInputStream extends ServletInputStream {

        private final InputStream sourceStream;

        /**
         * Create a DelegatingServletInputStream for the given source stream.
         * @param sourceStream the source stream (never <code>null</code>)
         */
        DelegatingServletInputStream(final InputStream sourceStream) {
            Assert.notNull(sourceStream, "Source InputStream must not be null");
            this.sourceStream = sourceStream;
        }


        public int read() throws IOException {
            return this.sourceStream.read();
        }

        public void close() throws IOException {
            super.close();
            this.sourceStream.close();
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(final ReadListener readListener) {

        }
    }
}