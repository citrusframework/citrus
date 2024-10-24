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

package org.citrusframework.functions;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.functions.core.RandomStringFunction;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

import static org.citrusframework.functions.Functions.changeDate;
import static org.citrusframework.functions.Functions.currentDate;
import static org.citrusframework.functions.Functions.decodeBase64;
import static org.citrusframework.functions.Functions.digestAuthHeader;
import static org.citrusframework.functions.Functions.encodeBase64;
import static org.citrusframework.functions.Functions.randomNumber;
import static org.citrusframework.functions.Functions.randomString;
import static org.citrusframework.functions.Functions.randomUUID;
import static org.citrusframework.functions.Functions.unixTimestamp;

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
    public void testChangeDate() {
        Assert.assertEquals(changeDate("01.01.2014", "+1y", context), "01.01.2015");
        Assert.assertEquals(changeDate("2014-01-01T12:00:00", "+1y", "yyyy-MM-dd'T'HH:mm:ss", context), "2015-01-01T12:00:00");
    }

    @Test
    public void testEncodeBase64() {
        Assert.assertEquals(encodeBase64("Foo", context), "Rm9v");
    }

    @Test
    public void testEncodeBase64WithCharset() {
        Assert.assertEquals(encodeBase64("Foo", StandardCharsets.UTF_8, context), "Rm9v");
    }

    @Test
    public void testDecodeBase64() {
        Assert.assertEquals(decodeBase64("Rm9v", context), "Foo");
    }

    @Test
    public void testDecodeBase64WithCharset() {
        Assert.assertEquals(decodeBase64("Rm9v", StandardCharsets.UTF_8, context), "Foo");
    }

    @Test
    public void testDigestAuthHeader() {
        digestAuthHeader("username", "password", "authRealm", "acegi", "POST", "http://localhost:8080", "citrus", "md5", context);
    }

    @Test
    public void testRandomUUID() {
        Assert.assertNotNull(randomUUID(context));
    }

    @Test
    public void testRandomNumber() {
        Assert.assertTrue(randomNumber(10L, context).length() > 9);
    }

    @Test
    public void testRandomNumberWithParams() {
        Assert.assertTrue(randomNumber(10L, true, context).length() > 9);
    }

    @Test
    public void testRandomString() {
        Assert.assertEquals(randomString(10L, context).length(), 10);
    }

    @Test
    public void testRandomStringWithParams() {
        Assert.assertEquals(randomString(10L, false, context).length(), 10);
        Assert.assertEquals(randomString(10L, RandomStringFunction.LOWERCASE, context).length(), 10);
        Assert.assertEquals(randomString(10L, RandomStringFunction.UPPERCASE, false, context).length(), 10);
    }

    @Test
    public void testUnixTimestamp() {
        Assert.assertEquals(String.valueOf(System.currentTimeMillis() / 1000L), unixTimestamp(context));
    }
}
