/*
 * Copyright 2006-2023 the original author or authors.
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

package org.citrusframework.endpoint.adapter.behavior;

import org.citrusframework.TestActionRunner;
import org.citrusframework.TestBehavior;
import org.citrusframework.message.MessageType;
import org.springframework.stereotype.Component;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

@Component("FooBarTestBehavior")
public class FooBarTestBehavior implements TestBehavior {

    @Override
    public void apply(TestActionRunner runner) {
        runner.$(receive()
                .endpoint("inboundDirectEndpoint")
                .message()
                .type(MessageType.PLAINTEXT)
                .body("<FooBarTestBehavior></FooBarTestBehavior>"));

        runner.$(send()
                .endpoint("inboundDirectEndpoint")
                .message()
                .body("<FooBarTestBehavior>OK</FooBarTestBehavior>"));

        runner.$(echo("FooBar TestBehavior OK!"));
    }
}
