/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.ftp.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.ftp.client.FtpEndpointConfiguration;
import org.citrusframework.server.AbstractServer;
import org.citrusframework.spi.Resource;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpServer extends AbstractServer {

    /** Apache ftp server */
    private FtpServerFactory serverFactory;
    private ListenerFactory listenerFactory;
    private UserManager userManager;
    private org.apache.ftpserver.FtpServer ftpServer;

    private final FtpEndpointConfiguration endpointConfiguration;

    /** Property file holding ftp user information */
    private Resource userManagerProperties;

    /** Do only start one instance after another, so we need a static lock object */
    private static final Object serverLock = new Object();

    /**
     * Default constructor using default endpoint configuration.
     */
    public FtpServer() {
        this(new FtpEndpointConfiguration());
    }

    /**
     * Constructor using endpoint configuration.
     * @param endpointConfiguration
     */
    public FtpServer(FtpEndpointConfiguration endpointConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    protected void startup() {
        synchronized (serverLock) {
            if (ftpServer == null) {
                listenerFactory.setPort(endpointConfiguration.getPort());
                serverFactory.addListener("default", listenerFactory.createListener());

                if (userManager != null) {
                    serverFactory.setUserManager(userManager);
                } else if (userManagerProperties != null) {
                    PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
                    userManagerFactory.setFile(userManagerProperties.getFile());
                    serverFactory.setUserManager(userManagerFactory.createUserManager());
                }

                NativeFileSystemFactory fileSystemFactory = new NativeFileSystemFactory();
                fileSystemFactory.setCreateHome(true);
                serverFactory.setFileSystem(fileSystemFactory);

                Map<String, Ftplet> ftpLets = new HashMap<String, Ftplet>();
                ftpLets.put("citrusFtpLet", new FtpServerFtpLet(getEndpointConfiguration(), getEndpointAdapter()));
                serverFactory.setFtplets(ftpLets);

                ftpServer = serverFactory.createServer();
            }

            try {
                ftpServer.start();
            } catch (FtpException e) {
                throw new CitrusRuntimeException(e);
            }
        }
    }

    @Override
    protected void shutdown() {
        if (ftpServer != null) {
            try {
                synchronized (serverLock) {
                    ftpServer.stop();
                }
            } catch (Exception e) {
                throw new CitrusRuntimeException(e);
            }
        }
    }

    @Override
    public FtpEndpointConfiguration getEndpointConfiguration() {
        return endpointConfiguration;
    }

    @Override
    public void initialize() {
        if (ftpServer == null) {
            if (serverFactory == null) {
                serverFactory = new FtpServerFactory();
            }

            if (listenerFactory == null) {
                listenerFactory = new ListenerFactory();
            }
        }

        super.initialize();
    }

    /**
     * Sets custom ftp server instance.
     * @param ftpServer
     */
    public void setFtpServer(org.apache.ftpserver.FtpServer ftpServer) {
        this.ftpServer = ftpServer;
    }

    /**
     * Gets ftp server instance.
     * @return
     */
    public org.apache.ftpserver.FtpServer getFtpServer() {
        return ftpServer;
    }

    /**
     * Sets custom user manager.
     * @param userManager
     */
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * Gets the user manager.
     * @return
     */
    public UserManager getUserManager() {
        return userManager;
    }

    /**
     * Sets the user manager properties.
     * @param userManagerProperties
     */
    public void setUserManagerProperties(Resource userManagerProperties) {
        this.userManagerProperties = userManagerProperties;
    }

    /**
     * Gets the user manager properties.
     * @return
     */
    public Resource getUserManagerProperties() {
        return userManagerProperties;
    }

    /**
     * Sets custom listener factory.
     * @param listenerFactory
     */
    public void setListenerFactory(ListenerFactory listenerFactory) {
        this.listenerFactory = listenerFactory;
    }

    /**
     * Gets the listener factory.
     * @return
     */
    public ListenerFactory getListenerFactory() {
        return listenerFactory;
    }
}
