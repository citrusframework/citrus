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

package com.consol.citrus.condition;

import com.consol.citrus.context.TestContext;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.easymock.EasyMock.*;

/**
 * @author Martin Maher
 * @since 2.4
 */
public class HttpConditionTest {

    private TestContext context = EasyMock.createMock(TestContext.class);
    private HttpURLConnection connection = EasyMock.createMock(HttpURLConnection.class);

    @Test
    public void isSatisfiedShouldSucceedWithValidUrl() throws Exception {
        String url = "http://www.citrusframework.org";
        String timeout = "3000";
        String httpResponseCode = "200";

        reset(connection);
        connection.setConnectTimeout(3000);
        expectLastCall().once();

        connection.setRequestMethod("HEAD");
        expectLastCall().once();

        expect(connection.getResponseCode()).andReturn(200).once();

        connection.disconnect();
        expectLastCall().once();
        replay(connection);

        HttpCondition testling = new HttpCondition() {
            @Override
            protected HttpURLConnection openConnection(URL url) {
                Assert.assertEquals(url.toExternalForm(), "http://www.citrusframework.org");

                return connection;
            }
        };

        testling.setUrl(url);
        testling.setTimeout(timeout);
        testling.setHttpResponseCode(httpResponseCode);

        reset(context);
        expect(context.resolveDynamicValue(url)).andReturn(url).anyTimes();
        expect(context.resolveDynamicValue(httpResponseCode)).andReturn(httpResponseCode).anyTimes();
        expect(context.resolveDynamicValue(timeout)).andReturn(timeout).anyTimes();
        replay(context);

        Assert.assertTrue(testling.isSatisfied(context));

        verify(connection);
    }

    @Test
    public void isSatisfiedShouldFailDueToInvalidUrl() throws Exception {
        String url = "http://127.0.0.1:13333/some/unknown/path";
        String httpResponseCode = "200";
        String timeout = "1000";
        HttpCondition testling = new HttpCondition();
        testling.setUrl(url);
        testling.setHttpResponseCode(httpResponseCode);
        testling.setTimeout(timeout);

        reset(context);
        expect(context.resolveDynamicValue(url)).andReturn(url).anyTimes();
        expect(context.resolveDynamicValue(httpResponseCode)).andReturn(httpResponseCode).anyTimes();
        expect(context.resolveDynamicValue(timeout)).andReturn(timeout).anyTimes();
        replay(context);

        Assert.assertFalse(testling.isSatisfied(context));
    }

}