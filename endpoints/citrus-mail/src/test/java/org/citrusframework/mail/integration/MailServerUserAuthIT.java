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

package org.citrusframework.mail.integration;

import org.citrusframework.TestCaseRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.mail.client.MailClient;
import org.citrusframework.mail.message.MailMessage;
import org.citrusframework.mail.server.MailServer;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Isolated("ITs share mail servers on fixed ports 25025/25050/25075 with GreenMail state")
public class MailServerUserAuthIT implements TestActionSupport {

    @Autowired
    private MailClient securedMailClient;

    @Autowired
    private MailServer securedMailServer;

    @Test
    @CitrusTest
    public void shouldAuthenticateUsers(@CitrusResource TestCaseRunner runner) {
        runner.run(send()
            .endpoint(securedMailClient)
                .message(MailMessage.request("foo@citrusframework.org", "bar@citrusframework.org", "Important!")
                        .body("Hello from foo-user!"))
        );

        runner.run(receive()
            .endpoint(securedMailServer)
            .message(MailMessage.request("foo@citrusframework.org", "bar@citrusframework.org", "Important!")
                    .body("Hello from foo-user!"))
        );
    }

    @Test
    @CitrusTest
    public void shouldFailOnUnauthenticatedUsers(@CitrusResource TestCaseRunner runner) {
        CitrusRuntimeException e = assertThrows(CitrusRuntimeException.class, () ->
            runner.run(send()
                .endpoint("unauthorizedMailClient")
                    .message(MailMessage.request("foo@citrusframework.org", "bar@citrusframework.org", "Important!")
                            .body("Hello from foo-user!"))
            )
        );
        assertEquals("Failed to send mail message!", e.getMessage());
    }
}
