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

package org.citrusframework.ftp.client;

import java.io.IOException;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScpClientTest {

    private static SshServer sshServer;

    @BeforeAll
    public static void setUp() throws IOException {
        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(0);
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());

        sshServer.setPasswordAuthenticator((username, password, session) -> "remote-password".equals(password));

        sshServer.start();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        sshServer.close();
    }

    @Test
    public void shouldLoginWithPreferredAuthentications() {
        ScpClient scpClient = createScpClient("password");

        assertDoesNotThrow(scpClient::connectAndLogin);
    }

    @Test
    public void shouldLoginWithDefaultPreferredAuthentications() {
        ScpClient scpClient = createScpClient(null);

        assertDoesNotThrow(scpClient::connectAndLogin);
    }

    @Test
    public void shouldOnlyUsePreferredAuthentications() {
        ScpClient scpClient = createScpClient("publickey");

        assertThrows(CitrusRuntimeException.class, scpClient::connectAndLogin);
    }

    private static ScpClient createScpClient(String preferredAuthentications) {
        ScpEndpointConfiguration endpointConfiguration = new ScpEndpointConfiguration();
        endpointConfiguration.setHost("localhost");
        endpointConfiguration.setPort(sshServer.getPort());
        endpointConfiguration.setUser("remote-username");
        endpointConfiguration.setPassword("remote-password");

        if (preferredAuthentications != null) {
            endpointConfiguration.setPreferredAuthentications(preferredAuthentications);
        }

        return new ScpClient(endpointConfiguration);
    }
}
