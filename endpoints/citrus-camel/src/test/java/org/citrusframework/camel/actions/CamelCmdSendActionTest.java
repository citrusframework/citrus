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

package org.citrusframework.camel.actions;

import org.citrusframework.camel.UnitTestSupport;
import org.citrusframework.camel.cli.CamelCli;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CamelCmdSendActionTest extends UnitTestSupport {

    @Mock
    private CamelCli camelCli;

    @Mock
    private ProcessAndOutput pao;

    @Test
    public void shouldSendMessage() {
        when(camelCli.send(any(String[].class))).thenAnswer(invocation -> {
            String[] args = (String[]) invocation.getRawArguments()[0];
            Assertions.assertArrayEquals(new String[] {
                    "my-route",
                    "--timeout",
                    "5000",
                    "--header",
                    "foo=bar",
                    "--body",
                    "\"Hello\"",
                    "--endpoint",
                    "direct:echo",
                    "--reply"
            }, args);

            return pao;
        });

        when(pao.getOutput()).thenReturn("Reply received - Hello");

        context.getReferenceResolver().bind("camelCli", camelCli);

        CamelCmdSendAction action = new CamelCmdSendAction.Builder()
                .integration("my-route")
                .timeout(5000)
                .header("foo", "bar")
                .body("Hello")
                .endpoint("direct:echo")
                .reply(true)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);
    }

    @Test
    public void shouldRestartSendCommandOnTransientCliFailure() {
        when(camelCli.send(any(String[].class)))
                .thenThrow(new CitrusRuntimeException("Send timeout while sending message via Camel CLI"))
                .thenReturn(pao);

        when(pao.getOutput()).thenReturn("Reply received - Hello");

        context.getReferenceResolver().bind("camelCli", camelCli);

        CamelCmdSendAction action = new CamelCmdSendAction.Builder()
                .integration("my-route")
                .body("Hello")
                .reply(true)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        verify(camelCli, times(2)).send(any(String[].class));
    }

    @Test
    public void shouldFailWhenSendCommandFailsRepeatedly() {
        when(camelCli.send(any(String[].class)))
                .thenThrow(new CitrusRuntimeException("Send timeout while sending message via Camel CLI"));

        context.getReferenceResolver().bind("camelCli", camelCli);

        CamelCmdSendAction action = new CamelCmdSendAction.Builder()
                .integration("my-route")
                .body("Hello")
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        CitrusRuntimeException error = Assertions.assertThrows(CitrusRuntimeException.class, () -> action.execute(context));
        Assertions.assertTrue(error.getMessage().contains("Send timeout"));
        verify(camelCli, times(2)).send(any(String[].class));
    }
}
