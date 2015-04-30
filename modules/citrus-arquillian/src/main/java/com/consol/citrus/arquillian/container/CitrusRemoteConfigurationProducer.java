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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
public class CitrusRemoteConfigurationProducer {

    @Inject
    @ApplicationScoped
    private InstanceProducer<CitrusConfiguration> configurationInstance;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusRemoteConfigurationProducer.class);

    public void configure(@Observes(precedence = CitrusExtensionConstants.REMOTE_CONFIG_PRECEDENCE) BeforeSuite event) {
        configurationInstance.set(CitrusConfiguration.from(getRemoteConfigurationProperties()));
    }

    private Properties getRemoteConfigurationProperties() {
        ClassLoader ctcl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });

        try {
            Properties props = new Properties();
            props.load(ctcl.getResourceAsStream(CitrusExtensionConstants.CITRUS_REMOTE_PROPERTIES));

            return props;
        } catch (IOException e) {
            return new Properties();
        }
    }
}
