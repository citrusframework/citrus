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

package org.citrusframework.base.functions;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.base.functions.core.RandomStringFunction;
import org.junit.jupiter.api.Test;

import static org.citrusframework.base.functions.Functions.changeDate;
import static org.citrusframework.base.functions.Functions.currentDate;
import static org.citrusframework.base.functions.Functions.decodeBase64;
import static org.citrusframework.base.functions.Functions.digestAuthHeader;
import static org.citrusframework.base.functions.Functions.encodeBase64;
import static org.citrusframework.base.functions.Functions.randomNumber;
import static org.citrusframework.base.functions.Functions.randomString;
import static org.citrusframework.base.functions.Functions.randomUUID;
import static org.citrusframework.base.functions.Functions.unixTimestamp;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals("01.01.2015", changeDate("01.01.2014", "+1y", context));
        assertEquals("2015-01-01T12:00:00", changeDate("2014-01-01T12:00:00", "+1y", "yyyy-MM-dd'T'HH:mm:ss", context));
    }

    @Test
    public void testEncodeBase64() {
        assertEquals("Rm9v", encodeBase64("Foo", context));
    }

    @Test
    public void testEncodeBase64WithCharset() {
        assertEquals("Rm9v", encodeBase64("Foo", StandardCharsets.UTF_8, context));
    }

    @Test
    public void testDecodeBase64() {
        assertEquals("Foo", decodeBase64("Rm9v", context));
    }

    @Test
    public void testDecodeBase64WithCharset() {
        assertEquals("Foo", decodeBase64("Rm9v", StandardCharsets.UTF_8, context));
    }

    @Test
    public void testDigestAuthHeader() {
        digestAuthHeader("username", "password", "authRealm", "acegi", "POST", "http://localhost:8080", "citrus", "md5", context);
    }

    @Test
    public void testRandomUUID() {
        assertNotNull(randomUUID(context));
    }

    @Test
    public void testRandomNumber() {
        assertTrue(randomNumber(10L, context).length() > 9);
    }

    @Test
    public void testRandomNumberWithParams() {
        assertTrue(randomNumber(10L, true, context).length() > 9);
    }

    @Test
    public void testRandomString() {
        assertEquals(10, randomString(10L, context).length());
    }

    @Test
    public void testRandomStringWithParams() {
        assertEquals(10, randomString(10L, false, context).length());
        assertEquals(10, randomString(10L, RandomStringFunction.NotationMethod.LOWERCASE.name(), context).length());
        assertEquals(10, randomString(10L, RandomStringFunction.NotationMethod.UPPERCASE.name(), false, context).length());
    }

    @Test
    public void testUnixTimestamp() {
        assertEquals(unixTimestamp(context), String.valueOf(System.currentTimeMillis() / 1000L));
    }
}
