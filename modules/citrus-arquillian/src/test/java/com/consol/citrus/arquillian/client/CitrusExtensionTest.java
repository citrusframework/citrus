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

import com.consol.citrus.arquillian.configuration.CitrusConfigurationProducer;
import com.consol.citrus.arquillian.enricher.CitrusInstanceProducer;
import com.consol.citrus.arquillian.enricher.CitrusTestEnricher;
import com.consol.citrus.arquillian.lifecycle.CitrusLifecycleHandler;
import org.easymock.EasyMock;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

public class CitrusExtensionTest {

    private CitrusExtension extension = new CitrusExtension();

    @Test
    public void testRegister() throws Exception {
        LoadableExtension.ExtensionBuilder extensionBuilder = EasyMock.createMock(LoadableExtension.ExtensionBuilder.class);

        expect(extensionBuilder.service(AuxiliaryArchiveAppender.class, CitrusArchiveAppender.class)).andReturn(extensionBuilder);
        expect(extensionBuilder.service(ApplicationArchiveProcessor.class, CitrusArchiveProcessor.class)).andReturn(extensionBuilder);

        expect(extensionBuilder.service(TestEnricher.class, CitrusTestEnricher.class)).andReturn(extensionBuilder).once();
        expect(extensionBuilder.observer(CitrusConfigurationProducer.class)).andReturn(extensionBuilder).once();
        expect(extensionBuilder.observer(CitrusInstanceProducer.class)).andReturn(extensionBuilder).once();
        expect(extensionBuilder.observer(CitrusLifecycleHandler.class)).andReturn(extensionBuilder).once();

        replay(extensionBuilder);

        extension.register(extensionBuilder);

        verify(extensionBuilder);
    }
}