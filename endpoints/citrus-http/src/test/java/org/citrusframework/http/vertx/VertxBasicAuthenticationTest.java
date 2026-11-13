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

package org.citrusframework.http.vertx;

import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxBasicAuthenticationTest {
    @DataProvider
    public Object[][] invalidFields() {
        return new Object[][] {{"", "p", "r"}, {"u:s", "p", "r"}, {"u", "p\n", "r"},
                {"u", "p", "r\r\nInjected: true"}, {"usér", "p", "r"}};
    }
    @Test(dataProvider = "invalidFields")
    public void invalidConfiguration(String user, String password, String realm) {
        expectThrows(IllegalArgumentException.class, () -> new VertxBasicAuthentication(user, password, realm));
    }
    @Test
    public void rejectsMissingMalformedAndDuplicateHeaders() {
        var auth = new VertxBasicAuthentication("u", "", "r");
        assertTrue(auth.accepts(List.of("bAsIc   dTo=")));
        for (List<String> headers : List.of(List.<String>of(), List.of("Basic"), List.of("Basic "),
                List.of("Basic %%%"), List.of("Basic dTo=, Basic dTo="), List.of("Basic dTo=", "Basic dTo="))) {
            assertFalse(auth.accepts(headers));
        }
    }
    @Test
    public void escapesRealmWithoutExposingCredentials() {
        var auth = new VertxBasicAuthentication("u", "secret", "a\"b\\c");
        assertEquals(auth.challenge(), "Basic realm=\"a\\\"b\\\\c\"");
        assertFalse(auth.toString().contains("secret"));
    }
}
