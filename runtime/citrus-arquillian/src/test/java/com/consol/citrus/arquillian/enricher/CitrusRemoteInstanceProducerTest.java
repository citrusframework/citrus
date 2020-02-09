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
import com.consol.citrus.arquillian.configuration.CitrusConfiguration;
import com.consol.citrus.arquillian.helper.InjectionHelper;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.mockito.Mockito.*;

public class CitrusRemoteInstanceProducerTest {

    private CitrusRemoteInstanceProducer citrusInstanceProducer = new CitrusRemoteInstanceProducer();

    private CitrusConfiguration configuration = CitrusConfiguration.from(new Properties());

    private InstanceProducer<Citrus> instanceProducer = Mockito.mock(InstanceProducer.class);
    private Instance<CitrusConfiguration> configurationInstance = Mockito.mock(Instance.class);

    @Test
    public void testCreateInstance() throws Exception {
        reset(instanceProducer, configurationInstance);

        when(configurationInstance.get()).thenReturn(configuration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Citrus citrus = (Citrus) invocation.getArguments()[0];
                Assert.assertNotNull(citrus);
                return null;
            }
        }).when(instanceProducer).set(any(Citrus.class));

        InjectionHelper.inject(citrusInstanceProducer, "citrusInstance", instanceProducer);
        InjectionHelper.inject(citrusInstanceProducer, "configurationInstance", configurationInstance);
        citrusInstanceProducer.beforeSuite(new BeforeSuite());

    }
}