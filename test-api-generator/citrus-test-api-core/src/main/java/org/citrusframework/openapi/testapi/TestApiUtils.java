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

package org.citrusframework.openapi.testapi;

import org.citrusframework.http.actions.HttpClientRequestActionBuilder.HttpMessageBuilderSupport;

import static org.citrusframework.util.StringUtils.isEmpty;

public final class TestApiUtils {

    private TestApiUtils() {
        //prevent instantiation of utility class
    }

    public static void addBasicAuthHeader(String username, String password, HttpMessageBuilderSupport messageBuilderSupport) {
        if (!isEmpty(username) && !isEmpty(password)) {
            messageBuilderSupport.header("Authorization", "Basic citrus:encodeBase64(" + username + ":" + password + ")");
        }
    }

    public static String mapXmlAttributeNameToJavaPropertyName(String attributeName) {
        if (isEmpty(attributeName)) {
            return attributeName;
        }

        if ("basicUsername".equals(attributeName)) {
            return "withBasicAuthUsername";
        } else if ("basicPassword".equals(attributeName)) {
            return "withBasicAuthPassword";
        }

        return attributeName;
    }
}
