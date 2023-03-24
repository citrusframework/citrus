/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.selenium.util;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class BrowserUtils {

    /**
     * Prevent instantiation.
     */
    private BrowserUtils() {
        super();
    }

    /**
     * Makes new unique URL to avoid IE caching.
     * @param url
     * @param unique
     * @return
     */
    public static String makeIECachingSafeUrl(String url, long unique) {
        if (url.contains("timestamp=")) {
            return url.replaceFirst("(.*)(timestamp=)(.*)([&#].*)", "$1$2" + unique + "$4")
                    .replaceFirst("(.*)(timestamp=)(.*)$", "$1$2" + unique);
        } else {
            return url.contains("?")
                    ? url + "&timestamp=" + unique
                    : url + "?timestamp=" + unique;
        }
    }
}
