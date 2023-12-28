/*
 *    Copyright 2018-2024 the original author or authors
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

package org.citrusframework.http.message;

import jakarta.servlet.http.Cookie;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;

/**
 * Class to convert Objects from or to Cookies
 * <p>
 * This class should be replaced as soon as possible by a third party cookie parser
 * The implementation of the Serializable interface is cause by the {@link HttpMessage} implementation of Serializable
 * and the implications from that.
 */
class CookieConverter implements Serializable {

    private static final String NAME = "Name";
    private static final String VALUE = "Value";
    private static final String SECURE = "Secure";
    private static final String PATH = "Path";
    private static final String DOMAIN = "Domain";
    private static final String MAX_AGE = "Max-Age";
    private static final String HTTP_ONLY = "HttpOnly";

    /**
     * Converts cookies from a HttpEntity into Cookie objects
     *
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
     * Converts a given cookie into a HTTP conform cookie String
     *
     * @param cookie the cookie to convert
     * @return The cookie string representation of the given cookie
     */
    String getCookieString(Cookie cookie) {
        StringBuilder builder = new StringBuilder();

        builder.append(cookie.getName());
        builder.append("=");
        builder.append(cookie.getValue());

        if (StringUtils.hasText(cookie.getPath())) {
            builder.append(";" + PATH + "=").append(cookie.getPath());
        }

        if (StringUtils.hasText(cookie.getDomain())) {
            builder.append(";" + DOMAIN + "=").append(cookie.getDomain());
        }

        if (cookie.getMaxAge() > 0) {
            builder.append(";" + MAX_AGE + "=").append(cookie.getMaxAge());
        }

        if (cookie.getSecure()) {
            builder.append(";" + SECURE);
        }

        if (cookie.isHttpOnly()) {
            builder.append(";" + HTTP_ONLY);
        }

        return builder.toString();
    }

    /**
     * Converts a cookie string from a http header value into a Cookie object
     *
     * @param cookieString The string to convert
     * @return The Cookie representation of the given String
     */
    private Cookie convertCookieString(String cookieString) {
        Cookie cookie = new Cookie(getCookieParam(NAME, cookieString), getCookieParam(VALUE, cookieString));

        if (cookieString.contains(PATH)) {
            cookie.setPath(getCookieParam(PATH, cookieString));
        }

        if (cookieString.contains(DOMAIN)) {
            cookie.setDomain(getCookieParam(DOMAIN, cookieString));
        }

        if (cookieString.contains(MAX_AGE)) {
            cookie.setMaxAge(parseInt(getCookieParam(MAX_AGE, cookieString)));
        }

        if (cookieString.contains(SECURE)) {
            cookie.setSecure(parseBoolean(getCookieParam(SECURE, cookieString)));
        }

        if (cookieString.contains(HTTP_ONLY)) {
            cookie.setHttpOnly(parseBoolean(getCookieParam(HTTP_ONLY, cookieString)));
        }

        return cookie;
    }

    /**
     * Extract cookie param from cookie string as it was provided by "Set-Cookie" header.
     *
     * @param param        The parameter to extract from the cookie string
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

        if (containsFlag(SECURE, param, cookieString) || containsFlag(HTTP_ONLY, param, cookieString)) {
            return TRUE.toString();
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

        throw new CitrusRuntimeException(
                format("Unable to get cookie argument '%s' from cookie String: %s", param, cookieString)
        );
    }

    private boolean containsFlag(String flag, String param, String cookieString) {
        return flag.equals(param) && cookieString.contains(flag);
    }
}
