/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.functions;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.functions.core.RandomStringFunction;
import org.citrusframework.functions.core.UnixTimestampFunction;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.functions.Functions.*;

public class FunctionsTest extends UnitTestSupport {

    @Test
    public void testCurrentDate() throws Exception {
        new SimpleDateFormat("dd.MM.yyyy").parse(currentDate(context));
    }

    @Test
    public void testCurrentDateFormat() throws Exception {
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(currentDate("yyyy-MM-dd'T'HH:mm:ss", context));
    }

    @Test
    public void testChangeDate() throws Exception {
        Assert.assertEquals(changeDate("01.01.2014", "+1y", context), "01.01.2015");
        Assert.assertEquals(changeDate("2014-01-01T12:00:00", "+1y", "yyyy-MM-dd'T'HH:mm:ss", context), "2015-01-01T12:00:00");
    }

    @Test
    public void testEncodeBase64() throws Exception {
        Assert.assertEquals(encodeBase64("Foo", context), "Rm9v");
    }

    @Test
    public void testEncodeBase64WithCharset() throws Exception {
        Assert.assertEquals(encodeBase64("Foo", StandardCharsets.UTF_8, context), "Rm9v");
    }

    @Test
    public void testDecodeBase64() throws Exception {
        Assert.assertEquals(decodeBase64("Rm9v", context), "Foo");
    }

    @Test
    public void testDecodeBase64WithCharset() throws Exception {
        Assert.assertEquals(decodeBase64("Rm9v", StandardCharsets.UTF_8, context), "Foo");
    }

    @Test
    public void testDigestAuthHeader() throws Exception {
        digestAuthHeader("username", "password", "authRealm", "acegi", "POST", "http://localhost:8080", "citrus", "md5", context);
    }

    @Test
    public void testRandomUUID() throws Exception {
        Assert.assertNotNull(randomUUID(context));
    }

    @Test
    public void testRandomNumber() throws Exception {
        Assert.assertTrue(randomNumber(10L, context).length() > 9);
    }

    @Test
    public void testRandomNumberWithParams() throws Exception {
        Assert.assertTrue(randomNumber(10L, true, context).length() > 9);
    }

    @Test
    public void testRandomString() throws Exception {
        Assert.assertEquals(randomString(10L, context).length(), 10);
    }

    @Test
    public void testRandomStringWithParams() throws Exception {
        Assert.assertEquals(randomString(10L, false, context).length(), 10);
        Assert.assertEquals(randomString(10L, RandomStringFunction.LOWERCASE, context).length(), 10);
        Assert.assertEquals(randomString(10L, RandomStringFunction.UPPERCASE, false, context).length(), 10);
    }

    @Test
    public void testUnixTimestamp() throws Exception {
        Assert.assertEquals(String.valueOf(System.currentTimeMillis() / 1000L), unixTimestamp(context));
    }
}
