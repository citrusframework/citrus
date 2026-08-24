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

package org.citrusframework.playwright.model;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.microsoft.playwright.Request;

import java.lang.reflect.Proxy;
import java.util.Map;

import org.testng.annotations.Test;

class NetworkRecordTest {

    @Test
    void shouldMaskSensitiveQueryParametersAndHeaders() {
        Request request = request("POST",
                "https://api.example.test/login?password=secret123&token=abc&safe=value",
                Map.of(
                        "Authorization", "Bearer secret-token",
                        "X-Api-Key", "secret-key",
                        "Accept", "application/json"));

        String report = NetworkRecord.request(request).format();

        assertTrue(report.contains("password=***"));
        assertTrue(report.contains("token=***"));
        assertTrue(report.contains("safe=value"));
        assertTrue(report.contains("Authorization=Bearer ***"));
        assertTrue(report.contains("X-Api-Key=***"));
        assertTrue(report.contains("Accept=application/json"));
        assertFalse(report.contains("secret123"));
        assertFalse(report.contains("secret-token"));
        assertFalse(report.contains("secret-key"));
    }

    private Request request(String method, String url, Map<String, String> headers) {
        return (Request) Proxy.newProxyInstance(Request.class.getClassLoader(), new Class<?>[]{Request.class},
                (proxy, invokedMethod, args) -> switch (invokedMethod.getName()) {
                    case "method" -> method;
                    case "url" -> url;
                    case "headers" -> headers;
                    default -> null;
                });
    }
}
