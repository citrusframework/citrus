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
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

public class CitrusExtensionTest {

    private CitrusExtension extension = new CitrusExtension();

    @Test
    public void testRegister() throws Exception {
        LoadableExtension.ExtensionBuilder extensionBuilder = Mockito.mock(LoadableExtension.ExtensionBuilder.class);

        when(extensionBuilder.service(AuxiliaryArchiveAppender.class, CitrusArchiveAppender.class)).thenReturn(extensionBuilder);
        when(extensionBuilder.service(ApplicationArchiveProcessor.class, CitrusArchiveProcessor.class)).thenReturn(extensionBuilder);

        when(extensionBuilder.service(TestEnricher.class, CitrusTestEnricher.class)).thenReturn(extensionBuilder);
        when(extensionBuilder.observer(CitrusConfigurationProducer.class)).thenReturn(extensionBuilder);
        when(extensionBuilder.observer(CitrusInstanceProducer.class)).thenReturn(extensionBuilder);
        when(extensionBuilder.observer(CitrusLifecycleHandler.class)).thenReturn(extensionBuilder);

        extension.register(extensionBuilder);

    }
}