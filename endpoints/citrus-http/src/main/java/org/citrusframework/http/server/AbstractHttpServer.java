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

package org.citrusframework.http.server;

import java.util.List;

import org.citrusframework.base.server.AbstractServer;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static java.util.Arrays.asList;
import static org.springframework.http.MediaType.valueOf;

/**
 * Transport neutral base class for Http server implementations. Holds all configuration options
 * that are independent of the underlying Http server technology such as the message converter,
 * the context path or the server side authentication and TLS settings.
 *
 * @since 5.2
 */
public abstract class AbstractHttpServer extends AbstractServer {

    /**
     * Server port
     */
    private int port = 8080;

    /**
     * Secure server port
     */
    private int securePort = 8443;

    /**
     * Context path
     */
    private String contextPath = "/";

    /**
     * Message converter
     */
    private HttpMessageConverter messageConverter = new HttpMessageConverter();

    /**
     * Should handle http cookies
     */
    private boolean handleCookies = false;

    /**
     * Should handle matrix encoded path parameters
     */
    private boolean removeSemicolonPathContent = true;

    /**
     * Default status code returned by http server
     */
    private int defaultStatusCode = HttpStatus.OK.value();

    /**
     * List of media types that should be handled with binary content processing
     */
    private List<MediaType> binaryMediaTypes = asList(
            MediaType.APPLICATION_OCTET_STREAM,
            MediaType.APPLICATION_PDF,
            MediaType.IMAGE_GIF,
            MediaType.IMAGE_JPEG,
            MediaType.IMAGE_PNG,
            valueOf("application/zip")
    );

    /**
     * When enabled default server features such as response compression will be applied
     */
    private boolean useDefaultFilters = HttpServerSettings.isUseDefaultFilters();

    /**
     * Optional server side authentication
     */
    private HttpAuthentication authentication;

    /**
     * Path spec the server side authentication applies to
     */
    private String securedPath = "/*";

    /**
     * Optional secure connection settings
     */
    private HttpSecureConnection secureConnection;

    /**
     * Gets the port.
     *
     * @return the port to get.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port.
     *
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the secure port.
     *
     * @return the secure port to get.
     */
    public int getSecurePort() {
        return securePort;
    }

    /**
     * Sets the secure port.
     *
     * @param securePort the secure port to set
     */
    public void setSecurePort(int securePort) {
        this.securePort = securePort;
    }

    /**
     * Gets the contextPath.
     *
     * @return the contextPath to get.
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the contextPath.
     *
     * @param contextPath the contextPath to set
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Gets the message converter.
     *
     * @return
     */
    public HttpMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the message converter.
     *
     * @param messageConverter
     */
    public void setMessageConverter(HttpMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Gets the handleCookies.
     *
     * @return
     */
    public boolean isHandleCookies() {
        return handleCookies;
    }

    /**
     * Sets the handleCookies.
     *
     * @param handleCookies
     */
    public void setHandleCookies(boolean handleCookies) {
        this.handleCookies = handleCookies;
    }

    /**
     * Gets the removeSemicolonPathContent.
     */
    public boolean isRemoveSemicolonPathContent() {
        return removeSemicolonPathContent;
    }

    /**
     * Sets the removeSemicolonPathContent.
     */
    public void setRemoveSemicolonPathContent(boolean removeSemicolonPathContent) {
        this.removeSemicolonPathContent = removeSemicolonPathContent;
    }

    /**
     * Gets the defaultStatusCode.
     *
     * @return
     */
    public int getDefaultStatusCode() {
        return defaultStatusCode;
    }

    /**
     * Sets the defaultStatusCode.
     *
     * @param defaultStatusCode
     */
    public void setDefaultStatusCode(int defaultStatusCode) {
        this.defaultStatusCode = defaultStatusCode;
    }

    /**
     * Gets the binaryMediaTypes.
     *
     * @return
     */
    public List<MediaType> getBinaryMediaTypes() {
        return binaryMediaTypes;
    }

    /**
     * Sets the binaryMediaTypes.
     *
     * @param binaryMediaTypes
     */
    public void setBinaryMediaTypes(List<MediaType> binaryMediaTypes) {
        this.binaryMediaTypes = binaryMediaTypes;
    }

    public boolean isUseDefaultFilters() {
        return useDefaultFilters;
    }

    public void setUseDefaultFilters(boolean useDefaultFilters) {
        this.useDefaultFilters = useDefaultFilters;
    }

    /**
     * Gets the server side authentication.
     *
     * @return
     */
    public HttpAuthentication getAuthentication() {
        return authentication;
    }

    /**
     * Sets the server side authentication.
     *
     * @param authentication
     */
    public void setAuthentication(HttpAuthentication authentication) {
        this.authentication = authentication;
    }

    /**
     * Gets the path spec the server side authentication applies to.
     *
     * @return
     */
    public String getSecuredPath() {
        return securedPath;
    }

    /**
     * Sets the path spec the server side authentication applies to.
     *
     * @param securedPath
     */
    public void setSecuredPath(String securedPath) {
        this.securedPath = securedPath;
    }

    /**
     * Gets the secure connection settings.
     *
     * @return
     */
    public HttpSecureConnection getSecureConnection() {
        return secureConnection;
    }

    /**
     * Sets the secure connection settings.
     *
     * @param secureConnection
     */
    public void setSecureConnection(HttpSecureConnection secureConnection) {
        this.secureConnection = secureConnection;
    }
}
