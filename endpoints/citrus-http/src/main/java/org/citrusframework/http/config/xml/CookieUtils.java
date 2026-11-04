package org.citrusframework.http.config.xml;

import java.util.List;

import jakarta.servlet.http.Cookie;
import org.citrusframework.http.message.HttpMessage;
import org.w3c.dom.Element;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

final class CookieUtils {

    private CookieUtils() {
        // Static utility class
    }

    static void setCookieElement(HttpMessage httpMessage, List<?> cookieElements) {
        for (Object item : cookieElements) {
            Element cookieElement = (Element) item;
            Cookie cookie = new Cookie(cookieElement.getAttribute("name"), cookieElement.getAttribute("value"));

            if (cookieElement.hasAttribute("path") && !cookieElement.getAttribute("path").isBlank()) {
                cookie.setPath(cookieElement.getAttribute("path"));
            }

            if (cookieElement.hasAttribute("domain") && !cookieElement.getAttribute("domain").isBlank()) {
                cookie.setDomain(cookieElement.getAttribute("domain"));
            }

            if (cookieElement.hasAttribute("max-age") && !cookieElement.getAttribute("max-age").isBlank()) {
                cookie.setMaxAge(parseInt(cookieElement.getAttribute("max-age")));
            }

            if (cookieElement.hasAttribute("secure") && !cookieElement.getAttribute("secure").isBlank()) {
                cookie.setSecure(parseBoolean(cookieElement.getAttribute("secure")));
            }

            if (cookieElement.hasAttribute("http-only") && !cookieElement.getAttribute("http-only").isBlank()) {
                cookie.setHttpOnly(parseBoolean(cookieElement.getAttribute("http-only")));
            }

            httpMessage.cookie(cookie);
        }
    }
}
