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

import com.consol.citrus.arquillian.annotation.InjectCitrus;
import com.consol.citrus.arquillian.container.CitrusRemoteExtension;
import com.consol.citrus.arquillian.enricher.CitrusInstanceProducer;
import com.consol.citrus.arquillian.enricher.CitrusTestEnricher;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.CachedAuxilliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
public class CitrusArchiveAppender extends CachedAuxilliaryArchiveAppender {

    @Override
    protected Archive<?> buildArchive() {
        return ShrinkWrap.create(JavaArchive.class)
            .addClass(InjectCitrus.class)
            .addClass(CitrusInstanceProducer.class)
            .addClass(CitrusTestEnricher.class)
            .addPackage(CitrusRemoteExtension.class.getPackage())
            .addPackage(ReflectionUtils.class.getPackage())
            .addAsServiceProvider(RemoteLoadableExtension.class, CitrusRemoteExtension.class);
    }
}
