/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.arquillian.container;

import com.consol.citrus.arquillian.CitrusExtensionConstants;
import com.consol.citrus.arquillian.configuration.CitrusConfiguration;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.*;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

/**
 * Special remote configuration producer operates on server side remote test execution. Reads Citrus remote
 * property file as classpath resource and constructs proper Citrus extension configuration.
 *
 * Citrus remote property is usually created from auxiliary archive that is loaded automatically with Citrus
 * Arquillian extension. If not use {@link com.consol.citrus.arquillian.CitrusExtensionConstants#CITRUS_REMOTE_PROPERTIES} property
 * file name as resource in your Shrinkwrap test deployment.
 *
 * @author Christoph Deppisch
 * @since 2.2
 */
public class CitrusRemoteConfigurationProducer {

    @Inject
    @ApplicationScoped
    private InstanceProducer<CitrusConfiguration> configurationInstance;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusRemoteConfigurationProducer.class);

    /**
     * Observes before suite event and tries to load Citrus remote extension properties.
     * @param event
     */
    public void configure(@Observes(precedence = CitrusExtensionConstants.REMOTE_CONFIG_PRECEDENCE) BeforeSuite event) {
        try {
            log.info("Producing Citrus remote configuration");
            configurationInstance.set(CitrusConfiguration.from(getRemoteConfigurationProperties()));
        } catch (Exception e) {
            log.error(CitrusExtensionConstants.CITRUS_EXTENSION_ERROR, e);
            throw e;
        }
    }

    /**
     * Reads configuration properties from remote property file that has been added to the auxiliary archive.
     * @return
     */
    private Properties getRemoteConfigurationProperties() {
        ClassLoader ctcl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });

        try {
            if (log.isDebugEnabled()) {
                log.debug("Loading Citrus remote extension properties ...");
            }

            Properties props = new Properties();
            InputStream inputStream = ctcl.getResourceAsStream(CitrusExtensionConstants.CITRUS_REMOTE_PROPERTIES);
            if (inputStream != null) {
                props.load(inputStream);
            }

            if (log.isDebugEnabled()) {
                log.debug("Successfully loaded Citrus remote extension properties");
            }

            return props;
        } catch (IOException e) {
            log.warn("Unable to load Citrus remote extension properties");

            return new Properties();
        }
    }
}
