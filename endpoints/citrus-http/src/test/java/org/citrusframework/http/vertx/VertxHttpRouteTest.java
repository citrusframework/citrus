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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxHttpRouteTest {
    @DataProvider
    public Object[][] invalidPaths() {
        return new Object[][] {{"/api/../secret"}, {"/api/%2e%2e/secret"},
                {"/api/a%2Fb"}, {"/api/a%5cb"}, {"/api/a%3bb"}, {"/api/%00"}, {"/api/%zz"}};
    }

    @Test(dataProvider = "invalidPaths")
    public void rejectsAmbiguousPaths(String path) {
        assertEquals(new VertxHttpRoute("/api", "/*").status(path), 400);
    }

    @DataProvider
    public Object[][] invalidConfiguration() {
        return new Object[][] {{"api", "/*"}, {"/api/", "/*"}, {"/../api", "/*"},
                {"/api", "orders"}, {"/api", "/or*ders"}, {"/api", "/../orders"},
                {"/api", "*."}, {"/api", "/orders?x=1"}};
    }

    @Test(dataProvider = "invalidConfiguration")
    public void rejectsInvalidConfiguration(String context, String mapping) {
        expectThrows(IllegalArgumentException.class, () -> new VertxHttpRoute(context, mapping));
    }

    @Test
    public void rootAliasAndParameters() {
        assertEquals(new VertxHttpRoute("", "/*").contextPath(), "");
        assertEquals(new VertxHttpRoute("/api", "/orders/*").status("/api/orders;x=1/42"), 200);
    }
}
