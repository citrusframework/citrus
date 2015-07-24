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

package com.consol.citrus.dsl.functions;

import com.consol.citrus.functions.core.*;

import java.nio.charset.Charset;
import java.util.*;

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
    public static String currentDate() {
        return new CurrentDateFunction().execute(Collections.<String>emptyList(), null);
    }

    /**
     * Runs current date function with arguments.
     * @return
     */
    public static String currentDate(String dateFormat) {
        return new CurrentDateFunction().execute(Collections.singletonList(dateFormat), null);
    }

    /**
     * Runs change date function with arguments.
     * @param date
     * @param dateOffset
     * @param dateFormat
     * @return
     */
    public static String changeDate(String date, String dateOffset, String dateFormat) {
        return new ChangeDateFunction().execute(Arrays.asList(date, dateOffset, dateFormat), null);
    }

    /**
     * Runs change date function with arguments.
     * @param date
     * @param dateOffset
     * @return
     */
    public static String changeDate(String date, String dateOffset) {
        return new ChangeDateFunction().execute(Arrays.asList(date, dateOffset), null);
    }

    /**
     * Runs create CData section function with arguments.
     * @return
     */
    public static String createCDataSection(String content) {
        return new CreateCDataSectionFunction().execute(Collections.singletonList(content), null);
    }

    /**
     * Runs encode base 64 function with arguments.
     * @return
     */
    public static String encodeBase64(String content) {
        return new EncodeBase64Function().execute(Collections.singletonList(content), null);
    }

    /**
     * Runs encode base 64 function with arguments.
     * @return
     */
    public static String encodeBase64(String content, Charset charset) {
        return new EncodeBase64Function().execute(Arrays.asList(content, charset.displayName()), null);
    }

    /**
     * Runs decode base 64 function with arguments.
     * @return
     */
    public static String decodeBase64(String content) {
        return new DecodeBase64Function().execute(Collections.singletonList(content), null);
    }

    /**
     * Runs decode base 64 function with arguments.
     * @return
     */
    public static String decodeBase64(String content, Charset charset) {
        return new DecodeBase64Function().execute(Arrays.asList(content, charset.displayName()), null);
    }

    /**
     * Runs create digest auth header function with arguments.
     * @return
     */
    public static String digestAuthHeader(String username, String password, String realm, String noncekey, String method, String uri, String opaque, String algorithm) {
        return new DigestAuthHeaderFunction().execute(Arrays.asList(username, password, realm, noncekey, method, uri, opaque, algorithm), null);
    }

    /**
     * Runs random UUID function with arguments.
     * @return
     */
    public static String randomUUID() {
        return new RandomUUIDFunction().execute(Collections.<String>emptyList(), null);
    }

    /**
     * Runs random number function with arguments.
     * @param length
     * @return
     */
    public static String randomNumber(Long length) {
        return new RandomNumberFunction().execute(Collections.singletonList(String.valueOf(length)), null);
    }

    /**
     * Runs random number function with arguments.
     * @param length
     * @param padding
     * @return
     */
    public static String randomNumber(Long length, boolean padding) {
        return new RandomNumberFunction().execute(Arrays.asList(String.valueOf(length), String.valueOf(padding)), null);
    }

    /**
     * Runs random string function with arguments.
     * @param numberOfLetters
     * @return
     */
    public static String randomString(Long numberOfLetters) {
        return new RandomStringFunction().execute(Collections.singletonList(String.valueOf(numberOfLetters)), null);
    }

    /**
     * Runs random string function with arguments.
     * @param numberOfLetters
     * @param useNumbers
     * @return
     */
    public static String randomString(Long numberOfLetters, boolean useNumbers) {
        return randomString(numberOfLetters, RandomStringFunction.MIXED, useNumbers);
    }

    /**
     * Runs random string function with arguments.
     * @param numberOfLetters
     * @param notationMethod
     * @param useNumbers
     * @return
     */
    public static String randomString(Long numberOfLetters, String notationMethod, boolean useNumbers) {
        return new RandomStringFunction().execute(Arrays.asList(String.valueOf(numberOfLetters), notationMethod, String.valueOf(useNumbers)), null);
    }

    /**
     * Runs random string function with arguments.
     * @param numberOfLetters
     * @param notationMethod
     * @return
     */
    public static String randomString(Long numberOfLetters, String notationMethod) {
        return new RandomStringFunction().execute(Arrays.asList(String.valueOf(numberOfLetters), String.valueOf(notationMethod)), null);
    }

    /**
     * Runs escape XML function with arguments.
     * @return
     */
    public static String escapeXml(String content) {
        return new EscapeXmlFunction().execute(Collections.singletonList(content), null);
    }
}
