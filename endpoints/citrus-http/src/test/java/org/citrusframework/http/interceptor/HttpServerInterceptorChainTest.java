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

package org.citrusframework.http.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test for the {@link HttpServerInterceptorChain}.
 */
public class HttpServerInterceptorChainTest extends AbstractTestNGUnitTest {

    private final List<String> invocations = new ArrayList<>();

    @BeforeMethod
    public void resetInvocations() {
        invocations.clear();
    }

    @Test
    public void shouldPassRequestToEndpointHandler() {
        HttpMessage request = new HttpMessage("payload");
        Message reply = new HttpMessage("reply");

        HttpServerInterceptorChain chain = new HttpServerInterceptorChain(List.of(), message -> {
            invocations.add("endpoint");
            return reply;
        });

        assertThat(chain.proceed(request)).isSameAs(reply);
        assertThat(invocations).containsExactly("endpoint");
    }

    @Test
    public void shouldInvokeInterceptorsInConfigurationOrder() {
        HttpServerInterceptorChain chain = new HttpServerInterceptorChain(
                List.of(
                        (request, next) -> {
                            invocations.add("first");
                            return next.proceed(request);
                        },
                        (request, next) -> {
                            invocations.add("second");
                            return next.proceed(request);
                        }),
                message -> {
                    invocations.add("endpoint");
                    return new HttpMessage("reply");
                });

        chain.proceed(new HttpMessage("payload"));

        assertThat(invocations).containsExactly("first", "second", "endpoint");
    }

    @Test
    public void shouldSupportShortCircuit() {
        HttpServerInterceptorChain chain = new HttpServerInterceptorChain(
                List.of((request, next) -> new HttpMessage("short circuit")),
                message -> {
                    invocations.add("endpoint");
                    return new HttpMessage("reply");
                });

        Message reply = chain.proceed(new HttpMessage("payload"));

        assertThat(reply.getPayload()).isEqualTo("short circuit");
        assertThat(invocations).isEmpty();
    }

    @Test
    public void shouldSupportChangingTheRequest() {
        HttpServerInterceptorChain chain = new HttpServerInterceptorChain(
                List.of((request, next) -> next.proceed(new HttpMessage("changed"))),
                message -> {
                    assertThat(message.getPayload()).isEqualTo("changed");
                    return new HttpMessage("reply");
                });

        chain.proceed(new HttpMessage("payload"));
    }

    @Test
    public void shouldSupportReplacingTheReply() {
        HttpServerInterceptorChain chain = new HttpServerInterceptorChain(
                List.of((request, next) -> new HttpMessage("replaced")),
                message -> new HttpMessage("reply"));

        Message reply = chain.proceed(new HttpMessage("payload"));

        assertThat(reply.getPayload()).isEqualTo("replaced");
    }

    @Test
    public void shouldPropagateException() {
        HttpServerInterceptorChain chain = new HttpServerInterceptorChain(
                List.of((request, next) -> {
                    throw new IllegalStateException("failed");
                }),
                message -> new HttpMessage("reply"));

        assertThatThrownBy(() -> chain.proceed(new HttpMessage("payload")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("failed");
    }

    @Test
    public void shouldPropagateEndpointException() {
        HttpServerInterceptorChain chain = new HttpServerInterceptorChain(
                List.of(),
                message -> {
                    throw new CitrusRuntimeException("endpoint failed");
                });

        assertThatThrownBy(() -> chain.proceed(new HttpMessage("payload")))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessage("endpoint failed");
    }
}
