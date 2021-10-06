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

package com.consol.citrus.arquillian.enricher;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusSpringContextProvider;
import com.consol.citrus.arquillian.CitrusExtensionConstants;
import com.consol.citrus.arquillian.configuration.CitrusConfiguration;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a new Citrus instance with basic configuration and sets result as application scoped
 * Arquillian resource.
 *
 * @author Christoph Deppisch
 * @since 2.2
 */
public class CitrusInstanceProducer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusInstanceProducer.class);

    @Inject
    private Instance<CitrusConfiguration> configurationInstance;

    @Inject
    @ApplicationScoped
    private InstanceProducer<Citrus> citrusInstance;

    /**
     * Before deploy executed before deployment on client.
     * @param event
     */
    public void beforeDeploy(@Observes(precedence = CitrusExtensionConstants.INSTANCE_PRECEDENCE) BeforeDeploy event) {
        try {
            if (!event.getDeployment().testable()) {
                log.info("Producing Citrus framework instance");
                citrusInstance.set(Citrus.newInstance(new CitrusSpringContextProvider(configurationInstance.get().getConfigurationClass())));
            }
        } catch (Exception e) {
            log.error(CitrusExtensionConstants.CITRUS_EXTENSION_ERROR, e);
            throw e;
        }
    }
}
