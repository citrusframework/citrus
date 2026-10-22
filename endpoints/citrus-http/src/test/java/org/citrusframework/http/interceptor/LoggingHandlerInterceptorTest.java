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

import org.citrusframework.http.controller.HttpMessageController;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class LoggingHandlerInterceptorTest {

    @Test
    public void testPreHandleWithBinaryRequestBody() throws Exception {
        RecordingLoggingHandlerInterceptor interceptor = new RecordingLoggingHandlerInterceptor();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/hello");
        request.setContentType("application/octet-stream");
        request.setContent(new byte[] { 1, 2, 3 });

        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());

        assertThat(interceptor.getRequestContent()).contains("<binary 3 bytes>");
        assertThat(interceptor.getRequestContent()).doesNotContain("[1, 2, 3]");
    }

    @Test
    public void testPostHandleWithBinaryResponseBody() throws Exception {
        RecordingLoggingHandlerInterceptor interceptor = new RecordingLoggingHandlerInterceptor();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/hello");
        MockHttpServletResponse response = new MockHttpServletResponse();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/octet-stream");

        HttpMessageController messageController = Mockito.mock(HttpMessageController.class);
        ResponseEntity<byte[]> responseEntity = ResponseEntity.status(HttpStatus.OK).headers(headers).body(new byte[] { 1, 2, 3 });
        Mockito.doReturn(responseEntity).when(messageController).getResponseCache(request);

        HandlerMethod handlerMethod = Mockito.mock(HandlerMethod.class);
        when(handlerMethod.getBean()).thenReturn(messageController);

        interceptor.postHandle(request, response, handlerMethod, new ModelAndView());

        assertThat(interceptor.getResponseContent()).contains("<binary 3 bytes>");
        assertThat(interceptor.getResponseContent()).doesNotContain("[1, 2, 3]");
    }

    /**
     * Interceptor that captures the rendered request and response content instead of writing it to the log.
     */
    private static final class RecordingLoggingHandlerInterceptor extends LoggingHandlerInterceptor {

        private String requestContent;
        private String responseContent;

        @Override
        public void handleRequest(String request) {
            this.requestContent = request;
        }

        @Override
        public void handleResponse(String response) {
            this.responseContent = response;
        }

        String getRequestContent() {
            return requestContent;
        }

        String getResponseContent() {
            return responseContent;
        }
    }
}
