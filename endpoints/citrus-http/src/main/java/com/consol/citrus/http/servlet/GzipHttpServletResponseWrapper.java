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

package com.consol.citrus.http.servlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

import org.springframework.http.HttpHeaders;

/**
 * Response wrapper wraps response output stream with gzip output stream. Write operations on that stream are
 * automatically compressed with gzip encoding.
 *
 * @author Christoph Deppisch
 * @since 2.6.2
 */
public class GzipHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private final HttpServletResponse origResponse;
    private ServletOutputStream outputStream;
    private PrintWriter printWriter;

    /**
     * Constructs a response adaptor wrapping the given response.
     *
     * @param response
     * @throws IllegalArgumentException if the response is null
     */
    public GzipHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
        origResponse = response;
    }

    /**
     * Finish response stream by closing.
     * @throws IOException
     */
    public void finish() throws IOException {
        if (printWriter != null) {
            printWriter.close();
        }

        if (outputStream != null) {
            outputStream.close();
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        if (outputStream != null) {
            outputStream.flush();
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (printWriter != null) {
            throw new IllegalStateException("Response writer already defined");
        }

        if (outputStream == null) {
            outputStream = new GzipServletOutputStream(origResponse);
        }

        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStream != null) {
            throw new IllegalStateException("Response output stream already defined");
        }

        if (printWriter == null) {
            outputStream = new GzipServletOutputStream(origResponse);
            printWriter = new PrintWriter(new OutputStreamWriter(outputStream, getResponse().getCharacterEncoding()));
        }

        return printWriter;
    }

    @Override
    public void setContentLength(int len) {
    }

    /**
     * Gzip enabled servlet output stream.
     */
    private class GzipServletOutputStream extends ServletOutputStream {
        private ByteArrayOutputStream bos;
        private GZIPOutputStream gzipStream;
        private final AtomicBoolean open;
        private HttpServletResponse response;
        private ServletOutputStream outputStream;

        /**
         * Default constructor using wrapped output stream.
         * @param response
         * @throws IOException
         */
        public GzipServletOutputStream(HttpServletResponse response) throws IOException {
            super();

            this.response = response;
            open = new AtomicBoolean(true);
            bos = new ByteArrayOutputStream();
            outputStream = response.getOutputStream();
            gzipStream = new GZIPOutputStream(bos);
        }

        @Override
        public void close() throws IOException {
            if (open.compareAndSet(true, false)) {
                gzipStream.finish();
                byte[] bytes = bos.toByteArray();
                response.addHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(bytes.length));
                response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
                outputStream.write(bytes);
                outputStream.flush();
                outputStream.close();
            }
        }

        @Override
        public void flush() throws IOException {
            if (!open.get()) {
                throw new IOException("Cannot flush a closed stream!");
            }

            gzipStream.flush();
        }

        @Override
        public void write(byte b[]) throws IOException {
            write(b, 0, b.length);
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            if (!open.get()) {
                throw new IOException("Stream closed!");
            }

            gzipStream.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            if (!open.get()) {
                throw new IOException("Stream closed!");
            }

            gzipStream.write(b);
        }

        @Override
        public boolean isReady() {
            return open.get();
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }
    }
}
