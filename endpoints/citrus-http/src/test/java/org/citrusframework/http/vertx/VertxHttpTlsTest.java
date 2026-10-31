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

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.http.HttpServerOptions;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxHttpTlsTest {
    @Test public void copiesProtocolsAndKeepsPasswordsOutOfDescription() {
        var protocols = new HashSet<>(Set.of("TLSv1.2"));
        var configuration = new VertxHttpTls(Path.of("server.p12"), "private-password").withProtocols(protocols);
        protocols.clear();
        var options = configuration.apply(new HttpServerOptions());
        assertTrue(options.isSsl()); assertFalse(options.isUseAlpn());
        assertEquals(options.getEnabledSecureTransportProtocols(), Set.of("TLSv1.2"));
        assertEquals(options.getClientAuth(), ClientAuth.NONE);
        assertFalse(configuration.toString().contains("private-password"));
        expectThrows(IllegalArgumentException.class, () -> configuration.withProtocols(Set.of()));
        expectThrows(IllegalArgumentException.class, () -> configuration.withProtocols(Set.of("TLSv1.1")));
    }
}
