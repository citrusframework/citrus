/*
 * Copyright 2006-2013 the original author or authors.
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

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.mail.client.MailClient;
import org.citrusframework.mail.message.MailMessage;
import org.citrusframework.mail.server.MailServer;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 */
public class MailServerUserAuthIT extends TestNGCitrusSpringSupport {

    @Autowired
    private MailClient securedMailClient;

    @Autowired
    private MailServer securedMailServer;

    @Test
    @CitrusTest
    public void shouldAuthenticateUsers() {
        run(send()
            .endpoint(securedMailClient)
                .message(MailMessage.request("foo@citrusframework.org", "bar@citrusframework.org", "Important!")
                        .body("Hello from foo-user!"))
        );

        run(receive()
            .endpoint(securedMailServer)
            .message(MailMessage.request("foo@citrusframework.org", "bar@citrusframework.org", "Important!")
                    .body("Hello from foo-user!"))
        );
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "Failed to send mail message!")
    @CitrusTest
    public void shouldFailOnUnauthenticatedUsers() {
        run(send()
            .endpoint("unauthorizedMailClient")
                .message(MailMessage.request("foo@citrusframework.org", "bar@citrusframework.org", "Important!")
                        .body("Hello from foo-user!"))
        );
    }
}
