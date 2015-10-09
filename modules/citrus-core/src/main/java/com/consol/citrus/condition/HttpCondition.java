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

package com.consol.citrus.condition;

import com.consol.citrus.context.TestContext;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Tests if a HTTP Endpoint is reachable. The test is successful if the endpoint responds with the expected response
 * code. By default a HTTP 200 response code is expected.
 *
 * @author Martin Maher
 * @since 2.4
 */
public class HttpCondition implements Condition {
    protected static final int SEC_IN_MILLISEC = 1000;
    public static final String DEFAULT_TIMEOUT = "1";
    public static final String DEFAULT_RESPONSE_CODE = "200"; // HTTP Success Code

    private String url;
    private String timeoutSeconds = DEFAULT_TIMEOUT;
    private String httpResponseCode = DEFAULT_RESPONSE_CODE;

    @Override
    public boolean isSatisfied(TestContext context) {
        return getHttpResponseCode(context) == testUrl(context);
    }

    private int testUrl(TestContext context) {
        int responseCode = -1;

        try {
            URL testUrl = getUrl(context);
            HttpURLConnection huc = (HttpURLConnection) testUrl.openConnection();
            huc.setConnectTimeout(getTimeoutMilliseconds(context));
            huc.setRequestMethod("HEAD");
            responseCode = huc.getResponseCode();
            huc.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseCode;
    }

    public String getUrl() {
        return url;
    }

    protected URL getUrl(TestContext context) throws MalformedURLException {
        URL url = new URL(context.resolveDynamicValue(this.url));
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTimeoutSeconds() {
        return timeoutSeconds;
    }

    private int getTimeoutMilliseconds(TestContext context) {
        return Integer.parseInt(context.resolveDynamicValue(timeoutSeconds)) * SEC_IN_MILLISEC;
    }

    public void setTimeoutSeconds(String timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getHttpResponseCode() {
        return httpResponseCode;
    }

    private int getHttpResponseCode(TestContext context) {
        return Integer.parseInt(context.resolveDynamicValue(httpResponseCode));
    }

    public void setHttpResponseCode(String httpResponseCode) {
        this.httpResponseCode = httpResponseCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HttpCondition that = (HttpCondition) o;

        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (timeoutSeconds != null ? !timeoutSeconds.equals(that.timeoutSeconds) : that.timeoutSeconds != null)
            return false;
        return !(httpResponseCode != null ? !httpResponseCode.equals(that.httpResponseCode) : that.httpResponseCode != null);

    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (timeoutSeconds != null ? timeoutSeconds.hashCode() : 0);
        result = 31 * result + (httpResponseCode != null ? httpResponseCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HttpCondition{" +
                "url='" + url + '\'' +
                ", timeoutSeconds='" + timeoutSeconds + '\'' +
                ", httpResponseCode='" + httpResponseCode + '\'' +
                '}';
    }
}
