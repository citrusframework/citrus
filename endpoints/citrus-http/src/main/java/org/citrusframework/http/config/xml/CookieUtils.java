package org.citrusframework.http.config.xml;

import jakarta.servlet.http.Cookie;
import org.citrusframework.http.message.HttpMessage;
import org.w3c.dom.Element;

import java.util.List;

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

            if (cookieElement.hasAttribute("path")) {
                cookie.setPath(cookieElement.getAttribute("path"));
            }

            if (cookieElement.hasAttribute("domain")) {
                cookie.setDomain(cookieElement.getAttribute("domain"));
            }

            if (cookieElement.hasAttribute("max-age")) {
                cookie.setMaxAge(parseInt(cookieElement.getAttribute("max-age")));
            }

            if (cookieElement.hasAttribute("secure")) {
                cookie.setSecure(parseBoolean(cookieElement.getAttribute("secure")));
            }

            httpMessage.cookie(cookie);
        }
    }
}
