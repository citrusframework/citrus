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
import com.consol.citrus.arquillian.enricher.CitrusInstanceProducer;
import com.consol.citrus.arquillian.enricher.CitrusTestEnricher;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
public class CitrusRemoteExtension implements RemoteLoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        if (Validate.classExists(CitrusExtensionConstants.CITRUS)) {
            builder.service(TestEnricher.class, CitrusTestEnricher.class);
            builder.observer(CitrusInstanceProducer.class);
        }
    }
}
