/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.condition;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests if a HTTP Endpoint is reachable. The test is successful if the endpoint responds with the expected response
 * code. By default a HTTP 200 response code is expected.
 *
 * @author Martin Maher
 * @since 2.4
 */
public class HttpCondition extends AbstractCondition {

    /** Http request URL to invoke for the condition check */
    private String url;
    private String timeout = "1000";

    /** Expected response code */
    private String httpResponseCode = "200";

    /** Request method */
    private String method = "HEAD";

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(HttpCondition.class);

    /**
     * Default constructor.
     */
    public HttpCondition() {
        super("http-check");
    }

    @Override
    public boolean isSatisfied(TestContext context) {
        return getHttpResponseCode(context) == invokeUrl(context);
    }

    @Override
    public String getSuccessMessage(TestContext context) {
        return String.format("Http condition success - request url '%s' did return expected status '%s'", getUrl(context), getHttpResponseCode(context));
    }

    @Override
    public String getErrorMessage(TestContext context) {
        return String.format("Failed to check Http condition - request url '%s' did not return expected status '%s'", getUrl(context), getHttpResponseCode(context));
    }

    /**
     * Invokes Http request URL and returns response code.
     * @param context The context to use for the URL invocation
     * @return The response code of the request
     */
    private int invokeUrl(TestContext context) {
        URL contextUrl = getUrl(context);
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Probing Http request url '%s'", contextUrl.toExternalForm()));
        }

        int responseCode = -1;

        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = openConnection(contextUrl);
            httpURLConnection.setConnectTimeout(getTimeout(context));
            httpURLConnection.setRequestMethod(context.resolveDynamicValue(method));

            responseCode = httpURLConnection.getResponseCode();
        } catch (IOException e) {
            logger.warn(String.format("Could not access Http url '%s' - %s", contextUrl.toExternalForm(), e.getMessage()));
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        return responseCode;
    }

    /**
     * Open Http url connection.
     * @param url The url to open
     * @return The opened connection
     */
    protected HttpURLConnection openConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }


    /**
     * Gets the request url with test variable support.
     * @param context The test context to get the url from
     * @return The extracted url
     */
    private URL getUrl(TestContext context) {
        try {
            return new URL(context.replaceDynamicContentInString(this.url));
        } catch (MalformedURLException e) {
            throw new CitrusRuntimeException("Invalid request url", e);
        }
    }

    /**
     * Gets the timeout in milliseconds.
     * @param context Test context to get the timeout from
     * @return The extracted timeout
     */
    private int getTimeout(TestContext context) {
        return Integer.parseInt(context.resolveDynamicValue(timeout));
    }

    /**
     * Gets the expected Http response code.
     * @param context The test context to get the response code from
     * @return The extracted response code
     */
    private int getHttpResponseCode(TestContext context) {
        return Integer.parseInt(context.resolveDynamicValue(httpResponseCode));
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getHttpResponseCode() {
        return httpResponseCode;
    }

    public void setHttpResponseCode(String httpResponseCode) {
        this.httpResponseCode = httpResponseCode;
    }

    @Override
    public String toString() {
        return "HttpCondition{" +
                "url='" + url + '\'' +
                ", timeout='" + timeout + '\'' +
                ", httpResponseCode='" + httpResponseCode + '\'' +
                ", method='" + method + '\'' +
                ", name='" + getName() + '\'' +
                '}';
    }
}
