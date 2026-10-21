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

import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;

/**
 * Test interceptor adding a custom response header. This way tests are able to verify that the
 * transport neutral {@link HttpServerInterceptor} has actually been applied to the request handling.
 */
public class HeaderServerInterceptor implements HttpServerInterceptor {

    public static final String HEADER_NAME = "X-Citrus-Server-Interceptor";
    public static final String HEADER_VALUE = "applied";

    @Override
    public Message intercept(HttpMessage request, Chain chain) {
        Message reply = chain.proceed(request);

        if (reply == null) {
            return null;
        }

        HttpMessage httpMessage = reply instanceof HttpMessage ? (HttpMessage) reply : new HttpMessage(reply);
        httpMessage.header(HEADER_NAME, HEADER_VALUE);
        return httpMessage;
    }
}
