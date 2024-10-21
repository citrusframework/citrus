/*
 * Copyright the original author or authors.
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

package org.citrusframework.http.message;

import static org.citrusframework.http.message.HttpMessageHeaders.HTTP_COOKIE_PREFIX;
import static org.citrusframework.http.message.HttpMessageUtils.getQueryParameterMap;
import static org.citrusframework.message.MessageHeaders.ID;
import static org.citrusframework.message.MessageHeaders.MESSAGE_TYPE;
import static org.citrusframework.message.MessageHeaders.TIMESTAMP;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import jakarta.servlet.http.Cookie;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @since 2.7
 */
public class HttpMessageUtilsTest {

    @Test
    public void testCopy() {
        HttpMessage from = new HttpMessage("fooMessage")
                            .header("X-Foo", "foo")
                            .addHeaderData("HeaderData");

        from.setName("FooMessage");

        HttpMessage to = new HttpMessage();

        HttpMessageUtils.copy(from, to);

        assertNotEquals(from.getId(), to.getId());
        assertEquals(to.getName(), "FooMessage");
        assertEquals(to.getPayload(String.class), "fooMessage");
        assertEquals(to.getHeaders().size(), 4L);
        assertNotNull(to.getHeader(ID));
        assertNotNull(to.getHeader(MESSAGE_TYPE));
        assertNotNull(to.getHeader(TIMESTAMP));
        assertEquals(to.getHeader("X-Foo"), "foo");
        assertEquals(to.getHeaderData().size(), 1L);
        assertEquals(to.getHeaderData().get(0), "HeaderData");
    }

    @Test
    public void testCopyPreventExistingOverwritePayload() {
        HttpMessage from = new HttpMessage("fooMessage")
                            .header("X-Foo", "foo")
                            .cookie(new Cookie("Foo", "fooCookie"))
                            .addHeaderData("HeaderData");

        from.setName("FooMessage");

        HttpMessage to = new HttpMessage("existingPayload")
                            .header("X-Existing", "existing")
                            .cookie(new Cookie("Existing", "existingCookie"))
                            .addHeaderData("ExistingHeaderData");

        to.setName("ExistingMessage");

        HttpMessageUtils.copy(from, to);

        assertNotEquals(from.getId(), to.getId());
        assertEquals(to.getName(), "FooMessage");
        assertEquals(to.getPayload(String.class), "fooMessage");
        assertEquals(to.getHeaders().size(), 7L);
        assertNotNull(to.getHeader(ID));
        assertNotNull(to.getHeader(MESSAGE_TYPE));
        assertNotNull(to.getHeader(TIMESTAMP));
        assertEquals(to.getHeader("X-Foo"), "foo");
        assertEquals(to.getHeader("X-Existing"), "existing");
        assertEquals(to.getHeader(HTTP_COOKIE_PREFIX + "Foo"), "Foo=fooCookie");
        assertEquals(to.getHeader(HTTP_COOKIE_PREFIX + "Existing"), "Existing=existingCookie");
        assertEquals(to.getHeaderData().size(), 2L);
        assertEquals(to.getHeaderData().get(0), "ExistingHeaderData");
        assertEquals(to.getHeaderData().get(1), "HeaderData");
    }

    @Test
    public void testConvertAndCopy() {
        Message from = new DefaultMessage("fooMessage")
                            .setHeader("X-Foo", "foo")
                            .addHeaderData("HeaderData");

        from.setName("FooMessage");

        HttpMessage to = new HttpMessage();

        HttpMessageUtils.copy(from, to);

        assertNotEquals(from.getId(), to.getId());
        assertEquals(to.getName(), "FooMessage");
        assertEquals(to.getPayload(String.class), "fooMessage");
        assertEquals(to.getHeader("X-Foo"), "foo");
        assertEquals(to.getHeaderData().size(), 1L);
        assertEquals(to.getHeaderData().get(0), "HeaderData");
    }

    @Test(dataProvider = "queryParamStrings")
    public void testQueryParamsExtraction(String queryParamString, Map<String, String> params) {
        HttpMessage message = new HttpMessage();
        message.queryParams(queryParamString);
        assertEquals(message.getQueryParams().size(), params.size());
        params.forEach((key, value) -> Assert.assertTrue(message.getQueryParams().get(key).contains(value)));
    }

    @DataProvider
    public Object[][] queryParamStrings() {
        return new Object[][] {
            new Object[] { "", Collections.emptyMap() },
            new Object[] { "key=value", Collections.singletonMap("key", "value") },
            new Object[] { "key1=value1,key2=value2", Stream.of(new String[] { "key1", "value1" },
                                                                new String[] { "key2", "value2" })
                                                        .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1])) },
            new Object[] { "key1,key2=value2", Stream.of(new String[] { "key1", "" },
                                                         new String[] { "key2", "value2" })
                                                        .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1])) },
            new Object[] { "key1,key2", Stream.of(new String[] { "key1", "" },
                                                  new String[] { "key2", "" })
                                                        .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1])) }
        };
    }


    @Test
    public void testGetQueryParameterMapWithValues() {
        HttpMessage httpMessage = new HttpMessage();
        httpMessage.queryParam("q1", "v1");
        httpMessage.queryParam("q1", "v2");
        httpMessage.queryParam("q2", "v3");
        httpMessage.queryParam("q2", "v4");
        httpMessage.queryParam("q3", "v5");

        Map<String, List<String>> queryParams = getQueryParameterMap(httpMessage);

        assertEquals(queryParams.size(), 3);
        List<String> q1Values = queryParams.get("q1");
        assertTrue(q1Values.contains("v1"));
        assertTrue(q1Values.contains("v2"));
        List<String> q2Values = queryParams.get("q2");
        assertTrue(q2Values.contains("v3"));
        assertTrue(q2Values.contains("v4"));
        List<String> q3Values = queryParams.get("q3");
        assertTrue(q3Values.contains("v5"));
    }

    @Test
    public void testGetQueryParameterMapWithNoValues() {
        HttpMessage httpMessage = new HttpMessage();

        Map<String, List<String>> queryParams = getQueryParameterMap(httpMessage);

        assertTrue(queryParams.isEmpty());
    }

    @Test
    public void testGetQueryParameterMapWithMissingValues() {
        HttpMessage httpMessage = new HttpMessage();
        httpMessage.queryParam("q1", "");
        httpMessage.queryParam("q2", "");
        httpMessage.queryParam("q3", "");

        Map<String, List<String>> queryParams = getQueryParameterMap(httpMessage);

        assertEquals(queryParams.size(), 3);
        List<String> q1Values = queryParams.get("q1");
        assertTrue(q1Values.contains(""));
        List<String> q2Values = queryParams.get("q2");
        assertTrue(q2Values.contains(""));
        List<String> q3Values = queryParams.get("q3");
        assertTrue(q3Values.contains(""));
    }

}
