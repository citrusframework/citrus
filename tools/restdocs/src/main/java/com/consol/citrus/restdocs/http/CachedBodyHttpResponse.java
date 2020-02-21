/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.restdocs.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Helper Http response wrapper implementation provides access to the response body for usage
 * in RestDoc converters. Delegates other method calls to original Http response instance.
 *
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CachedBodyHttpResponse implements ClientHttpResponse {

    private final ClientHttpResponse response;

    private byte[] body;

    public CachedBodyHttpResponse(ClientHttpResponse response) {
        this.response = response;
    }

    public HttpStatus getStatusCode() throws IOException {
        return this.response.getStatusCode();
    }

    public int getRawStatusCode() throws IOException {
        return this.response.getRawStatusCode();
    }

    public String getStatusText() throws IOException {
        return this.response.getStatusText();
    }

    public HttpHeaders getHeaders() {
        return this.response.getHeaders();
    }

    public InputStream getBody() throws IOException {
        if (this.body == null) {
            if (response.getBody() != null) {
                this.body = FileCopyUtils.copyToByteArray(response.getBody());
            } else {
                body = new byte[] {};
            }
        }
        return new ByteArrayInputStream(this.body);
    }

    public String getBodyAsString() throws IOException {
        if (this.body == null) {
            getBody();
        }

        return new String(body, Charset.forName("UTF-8"));
    }

    public byte[] getBodyAsBytes() throws IOException {
        if (this.body == null) {
            getBody();
        }

        return body;
    }

    public void close() {
        this.response.close();
    }
}
