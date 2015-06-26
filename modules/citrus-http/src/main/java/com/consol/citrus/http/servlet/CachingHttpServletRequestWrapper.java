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

package com.consol.citrus.http.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.springframework.util.FileCopyUtils;

/**
 * Caching wrapper saves request body data to cache when read.
 * @author Christoph Deppisch
 */
public class CachingHttpServletRequestWrapper extends HttpServletRequestWrapper {
    /** Cached request data initialized when first read from input stream */
    private byte[] body;
    
    /**
     * Default constructor using initial servlet request.
     * @param request
     */
    public CachingHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }
    
    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (body == null) {
            if (super.getInputStream() != null) {
                body = FileCopyUtils.copyToByteArray(super.getInputStream());
            } else {
                body = new byte[] {};
            }
        }
        return new RequestCachingInputStream();
    }
    
    /** Input stream uses cached request data */
    private final class RequestCachingInputStream extends ServletInputStream {
        private final ByteArrayInputStream is;

        private RequestCachingInputStream() {
            this.is = new ByteArrayInputStream(body);
        }

        @Override
        public boolean isFinished() {
            return is.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new CitrusRuntimeException("Unsupported operation");
        }

        @Override
        public int read() throws IOException {
            return is.read();
        }
    }
    
}