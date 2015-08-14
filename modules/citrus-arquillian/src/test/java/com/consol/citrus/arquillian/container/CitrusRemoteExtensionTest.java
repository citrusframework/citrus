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

import com.consol.citrus.arquillian.enricher.CitrusRemoteInstanceProducer;
import com.consol.citrus.arquillian.enricher.CitrusTestEnricher;
import com.consol.citrus.arquillian.lifecycle.CitrusRemoteLifecycleHandler;
import org.easymock.EasyMock;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

public class CitrusRemoteExtensionTest {

    private CitrusRemoteExtension extension = new CitrusRemoteExtension();

    @Test
    public void testRegister() throws Exception {
        LoadableExtension.ExtensionBuilder extensionBuilder = EasyMock.createMock(LoadableExtension.ExtensionBuilder.class);

        expect(extensionBuilder.service(TestEnricher.class, CitrusTestEnricher.class)).andReturn(extensionBuilder).once();
        expect(extensionBuilder.observer(CitrusRemoteConfigurationProducer.class)).andReturn(extensionBuilder).once();
        expect(extensionBuilder.observer(CitrusRemoteInstanceProducer.class)).andReturn(extensionBuilder).once();
        expect(extensionBuilder.observer(CitrusRemoteLifecycleHandler.class)).andReturn(extensionBuilder).once();

        replay(extensionBuilder);

        extension.register(extensionBuilder);

        verify(extensionBuilder);
    }
}