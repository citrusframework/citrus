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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.servlet.http.Cookie;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.citrusframework.http.message.HttpMessageHeaders.HTTP_COOKIE_PREFIX;
import static org.citrusframework.http.message.HttpMessageUtils.getQueryParameterMap;
import static org.citrusframework.message.MessageHeaders.ID;
import static org.citrusframework.message.MessageHeaders.MESSAGE_TYPE;
import static org.citrusframework.message.MessageHeaders.TIMESTAMP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertNotEquals( to.getId(),from.getId());
        assertEquals( "FooMessage",to.getName());
        assertEquals( "fooMessage",to.getPayload(String.class));
        assertEquals( 4L,to.getHeaders().size());
        assertNotNull(to.getHeader(ID));
        assertNotNull(to.getHeader(MESSAGE_TYPE));
        assertNotNull(to.getHeader(TIMESTAMP));
        assertEquals( "foo",to.getHeader("X-Foo"));
        assertEquals( 1L,to.getHeaderData().size());
        assertEquals( "HeaderData",to.getHeaderData().get(0));
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

        assertNotEquals( to.getId(),from.getId());
        assertEquals( "FooMessage",to.getName());
        assertEquals( "fooMessage",to.getPayload(String.class));
        assertEquals( 7L,to.getHeaders().size());
        assertNotNull(to.getHeader(ID));
        assertNotNull(to.getHeader(MESSAGE_TYPE));
        assertNotNull(to.getHeader(TIMESTAMP));
        assertEquals( "foo",to.getHeader("X-Foo"));
        assertEquals( "existing",to.getHeader("X-Existing"));
        assertEquals( "Foo=fooCookie",to.getHeader(HTTP_COOKIE_PREFIX + "Foo"));
        assertEquals( "Existing=existingCookie",to.getHeader(HTTP_COOKIE_PREFIX + "Existing"));
        assertEquals( 2L,to.getHeaderData().size());
        assertEquals( "ExistingHeaderData",to.getHeaderData().get(0));
        assertEquals( "HeaderData",to.getHeaderData().get(1));
    }

    @Test
    public void testConvertAndCopy() {
        Message from = new DefaultMessage("fooMessage")
                            .setHeader("X-Foo", "foo")
                            .addHeaderData("HeaderData");

        from.setName("FooMessage");

        HttpMessage to = new HttpMessage();

        HttpMessageUtils.copy(from, to);

        assertNotEquals( to.getId(),from.getId());
        assertEquals( "FooMessage",to.getName());
        assertEquals( "fooMessage",to.getPayload(String.class));
        assertEquals( "foo",to.getHeader("X-Foo"));
        assertEquals( 1L,to.getHeaderData().size());
        assertEquals( "HeaderData",to.getHeaderData().get(0));
    }

    @ParameterizedTest
    @MethodSource("queryParamStrings")
    public void testQueryParamsExtraction(String queryParamString, Map<String, String> params) {
        HttpMessage message = new HttpMessage();
        message.queryParams(queryParamString);
        assertEquals( params.size(),message.getQueryParams().size());
        params.forEach((key, value) -> Assertions.assertTrue(message.getQueryParams().get(key).contains(value)));
    }

    static Stream<Arguments> queryParamStrings() {
        return Stream.of(
            Arguments.of("", Collections.emptyMap()),
            Arguments.of("key=value", Collections.singletonMap("key", "value")),
            Arguments.of("key1=value1,key2=value2", Stream.of(new String[] { "key1", "value1" },
                                                                new String[] { "key2", "value2" })
                                                        .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1]))),
            Arguments.of("key1,key2=value2", Stream.of(new String[] { "key1", "" },
                                                          new String[] { "key2", "value2" })
                                                        .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1]))),
            Arguments.of("key1,key2", Stream.of(new String[] { "key1", "" },
                                                   new String[] { "key2", "" })
                                                        .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1])))
        );
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

        assertEquals( 3,queryParams.size());
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
    public void testGetQueryParameterMapWithEncodedCommaInValue() {
        HttpMessage httpMessage = new HttpMessage();
        httpMessage.queryParams("$select=Name%2CValue,$top=10");

        Map<String, List<String>> queryParams = getQueryParameterMap(httpMessage);

        assertEquals( 2,queryParams.size());
        List<String> selectValues = queryParams.get("$select");
        assertTrue(selectValues.contains("Name,Value"));
        List<String> topValues = queryParams.get("$top");
        assertTrue(topValues.contains("10"));
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

        assertEquals( 3,queryParams.size());
        List<String> q1Values = queryParams.get("q1");
        assertTrue(q1Values.contains(""));
        List<String> q2Values = queryParams.get("q2");
        assertTrue(q2Values.contains(""));
        List<String> q3Values = queryParams.get("q3");
        assertTrue(q3Values.contains(""));
    }
}
