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

import static org.easymock.EasyMock.*;

/**
 * @author Martin Maher
 * @since 2.4
 */
public class HttpConditionTest {

    private TestContext context = EasyMock.createMock(TestContext.class);

    @Test(enabled = false)
    // TODO Easymock cannot mock final classes (URL etc.) -> enable/rewrite test when mockito introduced
    public void isSatisfiedShouldSucceedWithValidUrl() throws Exception {
        String url = "http://www.google.com:80";
        String timeoutSeconds = "5";
        String httpResponseCode = "200";

        HttpCondition testling = new HttpCondition();
        testling.setUrl(url);
        testling.setTimeoutSeconds(timeoutSeconds);
        testling.setHttpResponseCode(httpResponseCode);

        reset(context);
        expect(context.resolveDynamicValue(url)).andReturn(url).anyTimes();
        expect(context.resolveDynamicValue(httpResponseCode)).andReturn(httpResponseCode).anyTimes();
        expect(context.resolveDynamicValue(timeoutSeconds)).andReturn(timeoutSeconds).anyTimes();
        replay(context);

        Assert.assertTrue(testling.isSatisfied(context));
    }

    @Test
    public void isSatisfiedShouldFailDueToInvalidUrl() throws Exception {
        String url = "http://127.0.0.1:13333/some/unknown/path";
        String httpResponseCode = "200";
        String timeoutSeconds = "1";
        HttpCondition testling = new HttpCondition();
        testling.setUrl(url);
        testling.setHttpResponseCode(httpResponseCode);
        testling.setTimeoutSeconds(timeoutSeconds);

        reset(context);
        expect(context.resolveDynamicValue(url)).andReturn(url).anyTimes();
        expect(context.resolveDynamicValue(httpResponseCode)).andReturn(httpResponseCode).anyTimes();
        expect(context.resolveDynamicValue(timeoutSeconds)).andReturn(timeoutSeconds).anyTimes();
        replay(context);

        Assert.assertFalse(testling.isSatisfied(context));
    }

}