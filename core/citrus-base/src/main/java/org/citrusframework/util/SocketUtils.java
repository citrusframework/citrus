/*
 * Copyright 2006-2010 the original author or authors.
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
import java.util.Random;
import javax.net.ServerSocketFactory;

import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Find an available TCP port randomly selected from given port range.
 */
public class SocketUtils {

    /**
     * The minimum value for port ranges used when finding an available TCP port.
     */
    private static final int PORT_RANGE_MIN = 1024;

    /**
     * The maximum value for port ranges used when finding an available TCP port.
     */
    private static final int PORT_RANGE_MAX = 65535;

    private static final int MAX_ATTEMPTS = 1000;

    private static final Random random = new Random(System.nanoTime());


    public static int findAvailableTcpPort() {
        return findAvailableTcpPort(PORT_RANGE_MIN);
    }

    public static int findAvailableTcpPort(int min) {
        if (min < PORT_RANGE_MIN) {
            throw new CitrusRuntimeException(String.format("Invalid minimum value for port range - ports range must be greater than %d", PORT_RANGE_MIN));
        }

        int candidatePort;
        int count = 0;
        do {
            if (++count > MAX_ATTEMPTS) {
                throw new CitrusRuntimeException(String.format("Could not find an available TCP port in the range [%d, %d] after %d attempts", min, PORT_RANGE_MAX, MAX_ATTEMPTS));
            }
            candidatePort = min + random.nextInt(PORT_RANGE_MAX - min + 1);
        }
        while (!isPortAvailable(candidatePort));

        return candidatePort;
    }


    /**
     * Check if the specified TCP port is currently available on localhost.
     */
    private static boolean isPortAvailable(int port) {
        try {
            ServerSocket serverSocket = ServerSocketFactory.getDefault()
                    .createServerSocket(port, 1, InetAddress.getByName("localhost"));
            serverSocket.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
