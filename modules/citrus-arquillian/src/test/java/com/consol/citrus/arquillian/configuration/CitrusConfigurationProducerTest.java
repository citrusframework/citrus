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
import com.consol.citrus.arquillian.helper.InjectionHelper;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.easymock.EasyMock.*;

public class CitrusConfigurationProducerTest {

    private CitrusConfigurationProducer configurationProducer = new CitrusConfigurationProducer();

    private ExtensionDef extension = EasyMock.createMock(ExtensionDef.class);
    private ArquillianDescriptor descriptor = EasyMock.createMock(ArquillianDescriptor.class);
    private InstanceProducer<CitrusConfiguration> instanceProducer = EasyMock.createMock(InstanceProducer.class);

    @Test
    public void testConfigure() throws Exception {
        reset(descriptor, extension, instanceProducer);

        expect(descriptor.getExtensions()).andReturn(Collections.singletonList(extension)).once();
        expect(extension.getExtensionName()).andReturn(CitrusExtensionConstants.CITRUS_EXTENSION_QUALIFIER).once();
        expect(extension.getExtensionProperties()).andReturn(Collections.<String, String>emptyMap()).once();

        instanceProducer.set(anyObject(CitrusConfiguration.class));
        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                CitrusConfiguration configuration = (CitrusConfiguration) getCurrentArguments()[0];
                Assert.assertNull(configuration.getCitrusVersion());
                Assert.assertTrue(configuration.isAutoPackage());

                return null;
            }
        });

        replay(descriptor, extension, instanceProducer);

        InjectionHelper.inject(configurationProducer, "configurationInstance", instanceProducer);
        configurationProducer.configure(descriptor);

        verify(descriptor, extension, instanceProducer);
    }
}