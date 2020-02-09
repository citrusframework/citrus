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

package com.consol.citrus.arquillian.configuration;

import com.consol.citrus.arquillian.CitrusExtensionConstants;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads Citrus extension properties from Arquillian descriptor and constructs proper configuration instance.
 *
 * @author Christoph Deppisch
 * @since 2.2
 */
public class CitrusConfigurationProducer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusConfigurationProducer.class);

    @Inject
    @ApplicationScoped
    private InstanceProducer<CitrusConfiguration> configurationInstance;

    public void configure(@Observes ArquillianDescriptor descriptor) {
        try {
            log.info("Producing Citrus configuration");
            configurationInstance.set(CitrusConfiguration.from(descriptor));
        } catch (Exception e) {
            log.error(CitrusExtensionConstants.CITRUS_EXTENSION_ERROR, e);
            throw e;
        }
    }
}
