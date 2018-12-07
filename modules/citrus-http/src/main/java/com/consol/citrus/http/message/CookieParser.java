/*
 *    Copyright 2018 the original author or authors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.consol.citrus.http.message;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.Cookie;
import java.util.LinkedList;
import java.util.List;

class CookieParser {

    private static final String NAME = "Name";
    private static final String VALUE = "Value";
    private static final String SECURE = "Secure";
    private static final String COMMENT = "Comment";
    private static final String PATH = "Path";
    private static final String DOMAIN = "Domain";
    private static final String MAX_AGE = "Max-Age";
    private static final String VERSION = "Version";

    /**
     * Converts cookies from a HttpEntity into Cookie objects
     * @param httpEntity The message to convert
     * @return A array of converted cookies
     */
    Cookie[] convertCookies(HttpEntity<?> httpEntity) {
        final List<Cookie> cookies = new LinkedList<>();

        List<String> inboundCookies = httpEntity.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (inboundCookies != null) {
            for (String cookieString : inboundCookies) {
                Cookie cookie = convertCookieString(cookieString);
                cookies.add(cookie);
            }
        }

        return cookies.toArray(new Cookie[0]);
    }

    /**
     * Converts a cookie string from a http header value into a Cookie object
     * @param cookieString The string to convert
     * @return The Cookie representation of the given String
     */
    private Cookie convertCookieString(String cookieString) {
        Cookie cookie = new Cookie(getCookieParam(NAME, cookieString), getCookieParam(VALUE, cookieString));

        if (cookieString.contains(COMMENT)) {
            cookie.setComment(getCookieParam(COMMENT, cookieString));
        }

        if (cookieString.contains(PATH)) {
            cookie.setPath(getCookieParam(PATH, cookieString));
        }

        if (cookieString.contains(DOMAIN)) {
            cookie.setDomain(getCookieParam(DOMAIN, cookieString));
        }

        if (cookieString.contains(MAX_AGE)) {
            cookie.setMaxAge(Integer.valueOf(getCookieParam(MAX_AGE, cookieString)));
        }

        if (cookieString.contains(SECURE)) {
            cookie.setSecure(Boolean.valueOf(getCookieParam(SECURE, cookieString)));
        }

        if (cookieString.contains(VERSION)) {
            cookie.setVersion(Integer.valueOf(getCookieParam(VERSION, cookieString)));
        }

        return cookie;
    }

    /**
     * Extract cookie param from cookie string as it was provided by "Set-Cookie" header.
     * @param param The parameter to extract from the cookie string
     * @param cookieString The cookie string from the cookie header to extract the parameter from
     * @return The value of the requested parameter
     */
    private String getCookieParam(String param, String cookieString) {
        if (param.equals(NAME)) {
            return cookieString.substring(0, cookieString.indexOf('='));
        }

        if (param.equals(VALUE)) {
            if (cookieString.contains(";")) {
                return cookieString.substring(cookieString.indexOf('=') + 1, cookieString.indexOf(';'));
            } else {
                return cookieString.substring(cookieString.indexOf('=') + 1);
            }
        }

        if(SECURE.equals(param) && cookieString.contains(SECURE)) {
            return String.valueOf(true);
        }

        if (cookieString.contains(param + '=')) {
            final int endParam = cookieString.indexOf(';', cookieString.indexOf(param + '='));
            final int beginIndex = cookieString.indexOf(param + '=') + param.length() + 1;
            if (endParam > 0) {
                return cookieString.substring(beginIndex, endParam);
            } else {
                return cookieString.substring(beginIndex);
            }
        }

        throw new CitrusRuntimeException(String.format(
                "Unable to get cookie argument '%s' from cookie String: %s", param, cookieString));
    }
}
