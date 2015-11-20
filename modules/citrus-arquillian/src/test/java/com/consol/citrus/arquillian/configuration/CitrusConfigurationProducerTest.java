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
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.*;

public class CitrusConfigurationProducerTest {

    private CitrusConfigurationProducer configurationProducer = new CitrusConfigurationProducer();

    private ExtensionDef extension = Mockito.mock(ExtensionDef.class);
    private ArquillianDescriptor descriptor = Mockito.mock(ArquillianDescriptor.class);
    private InstanceProducer<CitrusConfiguration> instanceProducer = Mockito.mock(InstanceProducer.class);

    @Test
    public void testConfigure() throws Exception {
        reset(descriptor, extension, instanceProducer);

        when(descriptor.getExtensions()).thenReturn(Collections.singletonList(extension));
        when(extension.getExtensionName()).thenReturn(CitrusExtensionConstants.CITRUS_EXTENSION_QUALIFIER);
        when(extension.getExtensionProperties()).thenReturn(Collections.<String, String>emptyMap());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                CitrusConfiguration configuration = (CitrusConfiguration) invocation.getArguments()[0];
                Assert.assertNull(configuration.getCitrusVersion());
                Assert.assertTrue(configuration.isAutoPackage());
                return null;
            }
        }).when(instanceProducer).set(any(CitrusConfiguration.class));

        InjectionHelper.inject(configurationProducer, "configurationInstance", instanceProducer);
        configurationProducer.configure(descriptor);

    }
}