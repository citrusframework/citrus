/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.jdbc.driver;

import com.consol.citrus.Citrus;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcDriver implements Driver {

    /** Client connects to db server */
    private HttpClient httpClient;

    /** Remote server url */
    private String serverUrl;

    /** Connection timeout */
    private int timeout = 5000;

    /** Driver URL prefix */
    private static final String URL_PREFIX = "jdbc:citrus:";

    public static final int MAJOR = Integer.valueOf(Citrus.getVersion().substring(0, Citrus.getVersion().indexOf('.')));
    public static final int MINOR = Integer.valueOf(Citrus.getVersion().substring(String.valueOf(MAJOR).length() + 1, String.valueOf(MAJOR).length() + 2));

    public static final JdbcDriver driverInstance = new JdbcDriver();

    static {
        try {
            DriverManager.registerDriver(driverInstance);
        } catch(Exception e) {
            LoggerFactory.getLogger(JdbcDriver.class).warn("Error registering jdbc driver", e);
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        JdbcConnection connection = null;

        if (acceptsURL(url)) {
            try {
                connectRemote(url, info);
                connection = new JdbcConnection(httpClient, serverUrl);
            } catch(Exception ex) {
                throw(new SQLException(ex.getMessage(), ex));
            }
        }

        return connection;
    }

    /**
     * This method makes the one time connection to the RMI server
     * @param url
     * @param info
     */
    private void connectRemote(String url, Properties info) throws SQLException {
        HttpResponse response = null;
        try {
            if (httpClient == null) {
                httpClient = HttpClients.custom()
                        .setDefaultRequestConfig(RequestConfig.copy(RequestConfig.DEFAULT)
                                .setConnectionRequestTimeout(timeout)
                                .setConnectTimeout(timeout)
                                .setSocketTimeout(timeout)
                                .build())
                        .build();

                URI uri = new URI(url.substring(URL_PREFIX.length()));
                serverUrl = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "");

                response = httpClient.execute(RequestBuilder.get(serverUrl + "/connection")
                        .addParameter("database", uri.getSchemeSpecificPart().substring(uri.getSchemeSpecificPart().lastIndexOf('/')))
                        .addParameters(info.entrySet()
                                            .stream()
                                            .map(entry -> new BasicNameValuePair(entry.getKey().toString(), entry.getValue() != null ? entry.getValue().toString() : ""))
                                            .collect(Collectors.toList()).toArray(new NameValuePair[info.size()]))
                        .build());

                if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                    throw new SQLException("Failed to connect to server: " + EntityUtils.toString(response.getEntity()));
                }
            }
        } catch(Exception ex) {
            throw new SQLException(ex);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith(URL_PREFIX);
    }

    @Override
    public int getMajorVersion() {
        return MAJOR;
    }

    @Override
    public int getMinorVersion() {
        return MINOR;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties loginProps) throws SQLException {
        return new DriverPropertyInfo[] {};
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getGlobal();
    }
}