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
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jdbc.server.RemoteConnection;
import com.consol.citrus.jdbc.server.RemoteDriver;
import org.slf4j.LoggerFactory;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Christoph Deppisch
 * @since 2.7.3
 */
public class JdbcDriver implements Driver {

    /** Remote driver */
    private static RemoteDriver remoteDriver = null;

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
        JdbcConnection localConnection = null;

        if (acceptsURL(url)) {
            try {
                connectRemote(url);

                RemoteConnection remoteConnection = remoteDriver.getConnection(info);
                localConnection = new JdbcConnection(remoteConnection);
            } catch(RemoteException ex) {
                throw(new SQLException("RemoteException: " + ex.getMessage()));
            } catch(Exception ex) {
                throw(new SQLException("LocalException: " + ex.getMessage()));
            }
        }

        return localConnection;
    }

    /**
     * This method makes the one time connection to the RMI server
     * @param url
     */
    private void connectRemote(String url) {
        try {
            if (remoteDriver == null) {
                remoteDriver = (RemoteDriver) Naming.lookup("rmi://" +
                        JdbcEndpointUtils.getHost(url.substring(URL_PREFIX.length())) + ":" +
                        JdbcEndpointUtils.getPort(url.substring(URL_PREFIX.length()), Registry.REGISTRY_PORT) + "/" +
                        JdbcEndpointUtils.getBinding(url.substring(URL_PREFIX.length())));
            }
        } catch(Exception ex) {
            throw new CitrusRuntimeException(ex);
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