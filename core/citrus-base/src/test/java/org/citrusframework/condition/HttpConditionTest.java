/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.condition;

import java.net.HttpURLConnection;
import java.net.URL;

import org.citrusframework.UnitTestSupport;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author Martin Maher
 * @since 2.4
 */
public class HttpConditionTest extends UnitTestSupport {

    private final HttpURLConnection connection = Mockito.mock(HttpURLConnection.class);

    @Test
    public void testValidUrl() throws Exception {
        String url = "https://citrusframework.org";
        String timeout = "3000";
        String httpResponseCode = "200";

        reset(connection);

        when(connection.getResponseCode()).thenReturn(200);

        HttpCondition testling = new HttpCondition() {
            @Override
            protected HttpURLConnection openConnection(URL url) {
                Assert.assertEquals(url.toExternalForm(), "https://citrusframework.org");

                return connection;
            }
        };

        testling.setUrl(url);
        testling.setTimeout(timeout);
        testling.setHttpResponseCode(httpResponseCode);

        Assert.assertTrue(testling.isSatisfied(context));

        verify(connection).setConnectTimeout(3000);
        verify(connection).setRequestMethod("HEAD");
        verify(connection).disconnect();
    }

    @Test
    public void testValidUrlVariableSupport() throws Exception {
        context.setVariable("url", "https://citrusframework.org");
        context.setVariable("timeout", "3000");
        context.setVariable("httpResponseCode", "200");

        reset(connection);

        when(connection.getResponseCode()).thenReturn(200);

        HttpCondition testling = new HttpCondition() {
            @Override
            protected HttpURLConnection openConnection(URL url) {
                Assert.assertEquals(url.toExternalForm(), "https://citrusframework.org");

                return connection;
            }
        };

        testling.setUrl("${url}");
        testling.setTimeout("${timeout}");
        testling.setHttpResponseCode("${httpResponseCode}");

        Assert.assertTrue(testling.isSatisfied(context));

        verify(connection).setConnectTimeout(3000);
        verify(connection).setRequestMethod("HEAD");
        verify(connection).disconnect();
    }

    @Test
    public void testInvalidUrl() {
        String url = "http://127.0.0.1:13333/some/unknown/path";
        String httpResponseCode = "200";
        String timeout = "1000";
        HttpCondition testling = new HttpCondition();
        testling.setUrl(url);
        testling.setHttpResponseCode(httpResponseCode);
        testling.setTimeout(timeout);

        Assert.assertFalse(testling.isSatisfied(context));
    }
}
