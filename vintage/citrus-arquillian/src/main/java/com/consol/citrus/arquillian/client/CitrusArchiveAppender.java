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

package com.consol.citrus.arquillian.client;

import com.consol.citrus.arquillian.CitrusExtensionConstants;
import com.consol.citrus.arquillian.configuration.CitrusConfiguration;
import com.consol.citrus.arquillian.container.CitrusRemoteConfigurationProducer;
import com.consol.citrus.arquillian.container.CitrusRemoteExtension;
import com.consol.citrus.arquillian.enricher.CitrusRemoteInstanceProducer;
import com.consol.citrus.arquillian.enricher.CitrusTestEnricher;
import com.consol.citrus.arquillian.lifecycle.CitrusRemoteLifecycleHandler;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.CachedAuxilliaryArchiveAppender;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.springframework.util.ReflectionUtils;

/**
 * Archive appender creates an auxilliary archive with all necessary Citrus extension classes and resources for
 * execution in remote application server.
 *
 * @author Christoph Deppisch
 * @since 2.2
 */
public class CitrusArchiveAppender extends CachedAuxilliaryArchiveAppender {

    @Inject
    private Instance<CitrusConfiguration> configurationInstance;

    @Override
    protected Archive<?> buildArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            .addClass(CitrusExtensionConstants.class)
            .addClass(CitrusConfiguration.class)
            .addClass(CitrusTestEnricher.class)
            .addClass(CitrusRemoteInstanceProducer.class)
            .addClass(CitrusRemoteLifecycleHandler.class)
            .addClass(CitrusRemoteConfigurationProducer.class)
            .addClass(CitrusRemoteExtension.class)
            .addPackage(ReflectionUtils.class.getPackage())
            .addAsResource(new StringAsset(configurationInstance.get().toString()), CitrusExtensionConstants.CITRUS_REMOTE_PROPERTIES)
            .addAsServiceProvider(RemoteLoadableExtension.class, CitrusRemoteExtension.class);
    }
}
