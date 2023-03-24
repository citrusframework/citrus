/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.ftp.server;

import org.citrusframework.ftp.client.FtpEndpointConfiguration;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpServerTest extends AbstractTestNGUnitTest {

    @Test
    public void startupAndShutdownTest() throws Exception {
        FtpEndpointConfiguration endpointConfiguration = new FtpEndpointConfiguration();
        FtpServer server = new FtpServer(endpointConfiguration);

        server.initialize();

        server.startup();
        server.shutdown();
    }
}
