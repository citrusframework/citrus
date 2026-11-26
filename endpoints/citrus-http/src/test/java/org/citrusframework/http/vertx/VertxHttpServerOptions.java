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

package org.citrusframework.http.vertx;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;

/**
 * Immutable, validated options for the test-only Vert.x HTTP server prototype.
 * A snapshot is taken at server startup, so later mutations only affect the next generation.
 */
public final class VertxHttpServerOptions {

    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 0;
    public static final int DEFAULT_STATUS_CODE = 200;
    public static final long DEFAULT_REPLY_TIMEOUT_MS = 5_000L;
    public static final long DEFAULT_STARTUP_TIMEOUT_MS = 30_000L;
    public static final long DEFAULT_SHUTDOWN_TIMEOUT_MS = 10_000L;
    public static final long DEFAULT_BODY_READ_TIMEOUT_MS = 5_000L;
    public static final long DEFAULT_MAX_BODY_SIZE_BYTES = 1024L * 1024L;
    public static final int DEFAULT_EXECUTOR_THREADS = 8;
    public static final int DEFAULT_MAX_CONCURRENT_EXCHANGES = 32;
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";

    private final String host;
    private final int port;
    private final int defaultStatusCode;
    private final long replyTimeoutMs;
    private final long startupTimeoutMs;
    private final long shutdownTimeoutMs;
    private final long bodyReadTimeoutMs;
    private final long maxBodySizeBytes;
    private final int executorThreads;
    private final int maxConcurrentExchanges;
    private final String charset;
    private final String contentType;
    private final List<MediaType> binaryMediaTypes;
    private final boolean handleCookies;
    private final boolean handleAttributeHeaders;
    private final boolean removeSemicolonPathContent;

    private VertxHttpServerOptions(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.defaultStatusCode = builder.defaultStatusCode;
        this.replyTimeoutMs = builder.replyTimeoutMs;
        this.startupTimeoutMs = builder.startupTimeoutMs;
        this.shutdownTimeoutMs = builder.shutdownTimeoutMs;
        this.bodyReadTimeoutMs = builder.bodyReadTimeoutMs;
        this.maxBodySizeBytes = builder.maxBodySizeBytes;
        this.executorThreads = builder.executorThreads;
        this.maxConcurrentExchanges = builder.maxConcurrentExchanges;
        this.charset = builder.charset;
        this.contentType = builder.contentType;
        this.binaryMediaTypes = Collections.unmodifiableList(new ArrayList<>(builder.binaryMediaTypes));
        this.handleCookies = builder.handleCookies;
        this.handleAttributeHeaders = builder.handleAttributeHeaders;
        this.removeSemicolonPathContent = builder.removeSemicolonPathContent;
    }

    public static VertxHttpServerOptions defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getDefaultStatusCode() {
        return defaultStatusCode;
    }

    public long getReplyTimeoutMs() {
        return replyTimeoutMs;
    }

    public long getStartupTimeoutMs() {
        return startupTimeoutMs;
    }

    public long getShutdownTimeoutMs() {
        return shutdownTimeoutMs;
    }

    public long getBodyReadTimeoutMs() {
        return bodyReadTimeoutMs;
    }

    public long getMaxBodySizeBytes() {
        return maxBodySizeBytes;
    }

    public int getExecutorThreads() {
        return executorThreads;
    }

    public int getMaxConcurrentExchanges() {
        return maxConcurrentExchanges;
    }

    public String getCharset() {
        return charset;
    }

    public String getContentType() {
        return contentType;
    }

    public List<MediaType> getBinaryMediaTypes() {
        return binaryMediaTypes;
    }

    public boolean isHandleCookies() {
        return handleCookies;
    }

    public boolean isHandleAttributeHeaders() {
        return handleAttributeHeaders;
    }

    public boolean isRemoveSemicolonPathContent() {
        return removeSemicolonPathContent;
    }

    public static final class Builder {
        private String host = DEFAULT_HOST;
        private int port = DEFAULT_PORT;
        private int defaultStatusCode = DEFAULT_STATUS_CODE;
        private long replyTimeoutMs = DEFAULT_REPLY_TIMEOUT_MS;
        private long startupTimeoutMs = DEFAULT_STARTUP_TIMEOUT_MS;
        private long shutdownTimeoutMs = DEFAULT_SHUTDOWN_TIMEOUT_MS;
        private long bodyReadTimeoutMs = DEFAULT_BODY_READ_TIMEOUT_MS;
        private long maxBodySizeBytes = DEFAULT_MAX_BODY_SIZE_BYTES;
        private int executorThreads = DEFAULT_EXECUTOR_THREADS;
        private int maxConcurrentExchanges = DEFAULT_MAX_CONCURRENT_EXCHANGES;
        private String charset = DEFAULT_CHARSET;
        private String contentType = DEFAULT_CONTENT_TYPE;
        private List<MediaType> binaryMediaTypes = new ArrayList<>(List.of(
                MediaType.APPLICATION_OCTET_STREAM,
                MediaType.APPLICATION_PDF,
                MediaType.IMAGE_GIF,
                MediaType.IMAGE_JPEG,
                MediaType.IMAGE_PNG,
                MediaType.valueOf("application/zip")));
        private boolean handleCookies;
        private boolean handleAttributeHeaders;
        private boolean removeSemicolonPathContent = true;

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder defaultStatusCode(int defaultStatusCode) {
            this.defaultStatusCode = defaultStatusCode;
            return this;
        }

        public Builder replyTimeoutMs(long replyTimeoutMs) {
            this.replyTimeoutMs = replyTimeoutMs;
            return this;
        }

        public Builder startupTimeoutMs(long startupTimeoutMs) {
            this.startupTimeoutMs = startupTimeoutMs;
            return this;
        }

        public Builder shutdownTimeoutMs(long shutdownTimeoutMs) {
            this.shutdownTimeoutMs = shutdownTimeoutMs;
            return this;
        }

        public Builder bodyReadTimeoutMs(long bodyReadTimeoutMs) {
            this.bodyReadTimeoutMs = bodyReadTimeoutMs;
            return this;
        }

        public Builder maxBodySizeBytes(long maxBodySizeBytes) {
            this.maxBodySizeBytes = maxBodySizeBytes;
            return this;
        }

        public Builder executorThreads(int executorThreads) {
            this.executorThreads = executorThreads;
            return this;
        }

        public Builder maxConcurrentExchanges(int maxConcurrentExchanges) {
            this.maxConcurrentExchanges = maxConcurrentExchanges;
            return this;
        }

        public Builder charset(String charset) {
            this.charset = charset;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder binaryMediaTypes(List<MediaType> binaryMediaTypes) {
            this.binaryMediaTypes = new ArrayList<>(binaryMediaTypes);
            return this;
        }

        public Builder handleCookies(boolean handleCookies) {
            this.handleCookies = handleCookies;
            return this;
        }

        public Builder handleAttributeHeaders(boolean handleAttributeHeaders) {
            this.handleAttributeHeaders = handleAttributeHeaders;
            return this;
        }

        public Builder removeSemicolonPathContent(boolean removeSemicolonPathContent) {
            this.removeSemicolonPathContent = removeSemicolonPathContent;
            return this;
        }

        public VertxHttpServerOptions build() {
            validate();
            return new VertxHttpServerOptions(this);
        }

        private void validate() {
            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("Vert.x prototype host must not be blank");
            }
            if (port < 0 || port > 65_535) {
                throw new IllegalArgumentException("Vert.x prototype port must be 0 (ephemeral) or 1-65535, was: " + port);
            }
            if (defaultStatusCode < 100 || defaultStatusCode > 599) {
                throw new IllegalArgumentException("Default status code must be 100-599, was: " + defaultStatusCode);
            }
            requirePositive(replyTimeoutMs, "replyTimeoutMs");
            requirePositive(startupTimeoutMs, "startupTimeoutMs");
            requirePositive(shutdownTimeoutMs, "shutdownTimeoutMs");
            requirePositive(bodyReadTimeoutMs, "bodyReadTimeoutMs");
            requirePositive(maxBodySizeBytes, "maxBodySizeBytes");
            if (executorThreads < 1) {
                throw new IllegalArgumentException("executorThreads must be >= 1");
            }
            if (maxConcurrentExchanges < 1) {
                throw new IllegalArgumentException("maxConcurrentExchanges must be >= 1");
            }
            if (charset == null || charset.isBlank()) {
                throw new IllegalArgumentException("charset must not be blank");
            }
            Charset.forName(charset);
            if (contentType == null || contentType.isBlank()) {
                throw new IllegalArgumentException("contentType must not be blank");
            }
            if (binaryMediaTypes == null) {
                throw new IllegalArgumentException("binaryMediaTypes must not be null");
            }
        }

        private void requirePositive(long value, String name) {
            if (value <= 0) {
                throw new IllegalArgumentException(name + " must be positive, was: " + value);
            }
        }
    }
}
