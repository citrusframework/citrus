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

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;

import org.citrusframework.context.TestContext;
import org.citrusframework.functions.core.ChangeDateFunction;
import org.citrusframework.functions.core.CurrentDateFunction;
import org.citrusframework.functions.core.DecodeBase64Function;
import org.citrusframework.functions.core.DigestAuthHeaderFunction;
import org.citrusframework.functions.core.EncodeBase64Function;
import org.citrusframework.functions.core.RandomNumberFunction;
import org.citrusframework.functions.core.RandomStringFunction;
import org.citrusframework.functions.core.RandomUUIDFunction;
import org.citrusframework.functions.core.ReadFileResourceFunction;
import org.citrusframework.functions.core.UrlDecodeFunction;
import org.citrusframework.functions.core.UrlEncodeFunction;
import org.citrusframework.functions.core.UnixTimestampFunction;

/**
 * @author Christoph Deppisch
 * @since 2.1
 */
public final class Functions {

    /**
     * Prevent instantiation.
     */
    private Functions() {
    }

    /**
     * Runs current date function with arguments.
     * @return
     */
    public static String currentDate(TestContext context) {
        return new CurrentDateFunction().execute(Collections.<String>emptyList(), context);
    }

    /**
     * Runs current date function with arguments.
     * @return
     */
    public static String currentDate(String dateFormat, TestContext context) {
        return new CurrentDateFunction().execute(Collections.singletonList(dateFormat), context);
    }

    /**
     * Runs change date function with arguments.
     * @param date
     * @param dateOffset
     * @param dateFormat
     * @return
     */
    public static String changeDate(String date, String dateOffset, String dateFormat, TestContext context) {
        return new ChangeDateFunction().execute(Arrays.asList(date, dateOffset, dateFormat), context);
    }

    /**
     * Runs change date function with arguments.
     * @param date
     * @param dateOffset
     * @return
     */
    public static String changeDate(String date, String dateOffset, TestContext context) {
        return new ChangeDateFunction().execute(Arrays.asList(date, dateOffset), context);
    }

    /**
     * Runs encode base 64 function with arguments.
     * @return
     */
    public static String encodeBase64(String content, TestContext context) {
        return new EncodeBase64Function().execute(Collections.singletonList(content), context);
    }

    /**
     * Runs encode base 64 function with arguments.
     * @return
     */
    public static String encodeBase64(String content, Charset charset, TestContext context) {
        return new EncodeBase64Function().execute(Arrays.asList(content, charset.displayName()), context);
    }

    /**
     * Runs decode base 64 function with arguments.
     * @return
     */
    public static String decodeBase64(String content, TestContext context) {
        return new DecodeBase64Function().execute(Collections.singletonList(content), context);
    }

    /**
     * Runs decode base 64 function with arguments.
     * @return
     */
    public static String decodeBase64(String content, Charset charset, TestContext context) {
        return new DecodeBase64Function().execute(Arrays.asList(content, charset.displayName()), context);
    }

    /**
     * Runs URL encode function with arguments.
     * @return
     */
    public static String urlEncode(String content, TestContext context) {
        return new UrlEncodeFunction().execute(Collections.singletonList(content), context);
    }

    /**
     * Runs URL encode function with arguments.
     * @return
     */
    public static String urlEncode(String content, Charset charset, TestContext context) {
        return new UrlEncodeFunction().execute(Arrays.asList(content, charset.displayName()), context);
    }

    /**
     * Runs URL decode function with arguments.
     * @return
     */
    public static String urlDecode(String content, TestContext context) {
        return new UrlDecodeFunction().execute(Collections.singletonList(content), context);
    }

    /**
     * Runs URL decode function with arguments.
     * @return
     */
    public static String urlDecode(String content, Charset charset, TestContext context) {
        return new UrlDecodeFunction().execute(Arrays.asList(content, charset.displayName()), context);
    }

    /**
     * Runs create digest auth header function with arguments.
     * @return
     */
    public static String digestAuthHeader(String username, String password, String realm, String noncekey, String method, String uri, String opaque, String algorithm, TestContext context) {
        return new DigestAuthHeaderFunction().execute(Arrays.asList(username, password, realm, noncekey, method, uri, opaque, algorithm), context);
    }

    /**
     * Runs random UUID function with arguments.
     * @return
     */
    public static String randomUUID(TestContext context) {
        return new RandomUUIDFunction().execute(Collections.<String>emptyList(), context);
    }

    /**
     * Runs random number function with arguments.
     * @param length
     * @return
     */
    public static String randomNumber(Long length, TestContext context) {
        return new RandomNumberFunction().execute(Collections.singletonList(String.valueOf(length)), context);
    }

    /**
     * Runs random number function with arguments.
     * @param length
     * @param padding
     * @return
     */
    public static String randomNumber(Long length, boolean padding, TestContext context) {
        return new RandomNumberFunction().execute(Arrays.asList(String.valueOf(length), String.valueOf(padding)), context);
    }

    /**
     * Runs random string function with arguments.
     * @param numberOfLetters
     * @return
     */
    public static String randomString(Long numberOfLetters, TestContext context) {
        return new RandomStringFunction().execute(Collections.singletonList(String.valueOf(numberOfLetters)), context);
    }

    /**
     * Runs random string function with arguments.
     * @param numberOfLetters
     * @param useNumbers
     * @return
     */
    public static String randomString(Long numberOfLetters, boolean useNumbers, TestContext context) {
        return randomString(numberOfLetters, RandomStringFunction.MIXED, useNumbers, context);
    }

    /**
     * Runs random string function with arguments.
     * @param numberOfLetters
     * @param notationMethod
     * @param useNumbers
     * @return
     */
    public static String randomString(Long numberOfLetters, String notationMethod, boolean useNumbers, TestContext context) {
        return new RandomStringFunction().execute(Arrays.asList(String.valueOf(numberOfLetters), notationMethod, String.valueOf(useNumbers)), context);
    }

    /**
     * Runs random string function with arguments.
     * @param numberOfLetters
     * @param notationMethod
     * @return
     */
    public static String randomString(Long numberOfLetters, String notationMethod, TestContext context) {
        return new RandomStringFunction().execute(Arrays.asList(String.valueOf(numberOfLetters), String.valueOf(notationMethod)), context);
    }

    /**
     * Reads the file resource and returns the complete file content.
     * @param filePath
     * @return
     */
    public static String readFile(String filePath, TestContext context) {
        return new ReadFileResourceFunction().execute(Collections.singletonList(filePath), context);
    }

    /**
     * Runs unix timestamp function with arguments.
     * @return
     */
    public static String unixTimestamp(TestContext context) {
        return new UnixTimestampFunction().execute(Collections.<String>emptyList(), context);
    }
}
