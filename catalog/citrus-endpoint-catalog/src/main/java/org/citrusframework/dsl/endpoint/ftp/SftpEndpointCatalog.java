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

package org.citrusframework.dsl.endpoint.ftp;

import org.citrusframework.ftp.client.SftpClientBuilder;
import org.citrusframework.ftp.endpoint.builder.SftpEndpoints;
import org.citrusframework.ftp.server.SftpServerBuilder;

public class SftpEndpointCatalog {

    /**
     * Private constructor setting the client and server builder implementation.
     */
    private SftpEndpointCatalog() {
        // prevent direct instantiation
    }

    public static SftpEndpointCatalog sftp() {
        return new SftpEndpointCatalog();
    }

    /**
     * Gets the client builder.
     * @return
     */
    public SftpClientBuilder client() {
        return SftpEndpoints.sftp().client();
    }

    /**
     * Gets the client builder.
     * @return
     */
    public SftpServerBuilder server() {
        return SftpEndpoints.sftp().server();
    }
}
