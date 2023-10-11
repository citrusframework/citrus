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

package org.citrusframework.http.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.citrusframework.CitrusSettings;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Caching wrapper saves request body data to cache when read.
 */
public class CachingHttpServletRequestWrapper extends HttpServletRequestWrapper {
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CachingHttpServletRequestWrapper.class);

    /** Cached request data initialized when first read from input stream */
    private byte[] body;

    /**
     * Default constructor using initial servlet request.
     * @param request The request to wrap
     */
    @SuppressWarnings("WeakerAccess")
    public CachingHttpServletRequestWrapper(final HttpServletRequest request) {
        super(request);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        final Map<String, String[]> params = new HashMap<>(super.getParameterMap());

        MediaType contentType = Optional.ofNullable(getContentType())
                .map(mediaType -> {
                    try {
                        return MediaType.valueOf(mediaType);
                    } catch (InvalidMediaTypeException e) {
                        logger.warn(String.format("Failed to parse content type '%s' - using default media type '%s'",
                                getContentType(), MediaType.ALL_VALUE), e);
                        return MediaType.ALL;
                    }
                })
                .orElse(MediaType.ALL);

        Charset charset = Optional.ofNullable(contentType.getCharset())
                                  .orElse(Charset.forName(CitrusSettings.CITRUS_FILE_ENCODING));

        if (RequestMethod.POST.name().equals(getMethod()) || RequestMethod.PUT.name().equals(getMethod())) {
            if (new MediaType(contentType.getType(), contentType.getSubtype()).equals(MediaType.APPLICATION_FORM_URLENCODED)) {
                try {
                    fillParams(params, new String(FileUtils.copyToByteArray(getInputStream()), charset), charset);
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read request body", e);
                }
            }
        } else {
            fillParams(params, getQueryString(), charset);
        }

        return params;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (body == null) {
            if (super.getInputStream() != null) {
                body = FileUtils.copyToByteArray(super.getInputStream());
            } else {
                body = new byte[] {};
            }
        }
        return new RequestCachingInputStream();
    }

    /**
     * Adds parameter name value paris extracted from given query string.
     * @param params The parameter map to alter
     * @param queryString The query string to extract the values from
     * @param charset
     */
    private void fillParams(final Map<String, String[]> params, final String queryString, Charset charset) {
        if (StringUtils.hasText(queryString)) {
            final StringTokenizer tokenizer = new StringTokenizer(queryString, "&");
            while (tokenizer.hasMoreTokens()) {
                final String[] nameValuePair = tokenizer.nextToken().split("=");

                String paramName = nameValuePair[0];
                if (params.containsKey(paramName)) {
                    continue;
                }

                String paramValue;
                if (nameValuePair.length > 1) {
                    paramValue = nameValuePair[1];
                } else {
                    paramValue = "";
                }

                try {
                    params.put(URLDecoder.decode(paramName, charset.name()),
                            new String[] { URLDecoder.decode(paramValue, charset.name()) });
                } catch (final UnsupportedEncodingException e) {
                    throw new CitrusRuntimeException(String.format(
                            "Failed to decode query param value '%s=%s'",
                            paramName,
                            paramValue), e);
                }
            }
        }
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
        public void setReadListener(final ReadListener readListener) {
            throw new CitrusRuntimeException("Unsupported operation");
        }

        @Override
        public int read() {
            return is.read();
        }
    }

}
