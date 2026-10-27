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

package org.citrusframework.util;

import java.net.InetAddress;
import java.net.ServerSocket;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SocketUtilsTest {

    @Test
    public void shouldFindPortWithinRange() {
        for (int i = 0; i < 100; i++) {
            int port = SocketUtils.findAvailableTcpPort(20000, 20100);
            assertTrue(port >= 20000 && port <= 20100, "Port " + port + " is out of range");
        }
    }

    @Test
    public void shouldFindPortInSinglePortRange() throws Exception {
        int port = SocketUtils.findAvailableTcpPort(20000, 32767);
        assertEquals(port, SocketUtils.findAvailableTcpPort(port, port));
    }

    @Test
    public void shouldFailWhenSinglePortRangeIsTaken() throws Exception {
        int port = SocketUtils.findAvailableTcpPort(20000, 32767);
        try (ServerSocket ignored = new ServerSocket(port, 1, InetAddress.getByName("localhost"))) {
            assertThrows(CitrusRuntimeException.class, () -> SocketUtils.findAvailableTcpPort(port, port));
        }
    }

    @Test
    public void shouldRejectInvalidRange() {
        assertThrows(CitrusRuntimeException.class, () -> SocketUtils.findAvailableTcpPort(1023, 2000));
        assertThrows(CitrusRuntimeException.class, () -> SocketUtils.findAvailableTcpPort(2000, 65536));
        assertThrows(CitrusRuntimeException.class, () -> SocketUtils.findAvailableTcpPort(3000, 2000));
    }
}
