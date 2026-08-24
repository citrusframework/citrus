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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.List;
import java.util.Map;

import org.citrusframework.log.CitrusLogSettings;
import org.testng.annotations.Test;

class SecretPatternRedactorTest {

    @Test
    void shouldMaskBuiltInAndConfiguredSecretNames() {
        SecretPatternRedactor redactor = new SecretPatternRedactor(List.of("tenant-secret", "clientid"));
        String mask = SecretPatternRedactor.MASK;

        assertEquals("https://api.example.test/orders?token=" + mask + "&tenant-secret=" + mask + "&safe=value",
                redactor.sanitizeUrl("https://api.example.test/orders?token=abc&tenant-secret=hidden&safe=value"));
        assertEquals(Map.of(
                        "Authorization", "Bearer " + mask,
                        "X-ClientId", mask,
                        "Accept", "application/json"),
                redactor.sanitizeHeaders(Map.of(
                        "Authorization", "Bearer secret-token",
                        "X-ClientId", "client-secret",
                        "Accept", "application/json")));

        String report = redactor.sanitizeUrl("https://api.example.test?clientid=123");
        assertFalse(report.contains("123"));
    }

    @Test
    void shouldMaskArbitraryTextContainingNamedSecrets() {
        SecretPatternRedactor redactor = new SecretPatternRedactor(List.of("session"));

        String sanitized = redactor.sanitizeText("cookie=session=abc123; token=secret; visible=value");

        assertEquals("cookie=" + SecretPatternRedactor.MASK + "; token="
                + SecretPatternRedactor.MASK + "; visible=value", sanitized);
    }

    @Test
    void shouldHonorCitrusLogMaskConfiguration() {
        assertEquals(CitrusLogSettings.getLogMaskValue(), SecretPatternRedactor.MASK);

        // secretKey is a citrus.logger default keyword but not a connector built-in
        SecretPatternRedactor redactor = new SecretPatternRedactor();
        assertEquals("secretkey=" + SecretPatternRedactor.MASK + "; visible=value",
                redactor.sanitizeText("secretkey=hunter2; visible=value"));
    }
}
