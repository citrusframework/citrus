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

package org.citrusframework.citrus.arquillian.container;

import org.citrusframework.citrus.arquillian.configuration.CitrusConfiguration;
import org.citrusframework.citrus.arquillian.helper.InjectionHelper;
import org.citrusframework.citrus.config.CitrusSpringConfig;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class CitrusRemoteConfigurationProducerTest {

    private CitrusRemoteConfigurationProducer configurationProducer = new CitrusRemoteConfigurationProducer();
    private InstanceProducer<CitrusConfiguration> instanceProducer = Mockito.mock(InstanceProducer.class);

    @Test
    public void testConfigure() throws Exception {
        reset(instanceProducer);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                CitrusConfiguration configuration = (CitrusConfiguration) invocation.getArguments()[0];
                Assert.assertEquals(configuration.getCitrusVersion(), "latest");
                Assert.assertTrue(configuration.isAutoPackage());
                Assert.assertEquals(configuration.getSuiteName(), "remoteSuite");
                Assert.assertEquals(configuration.getConfigurationClass(), CitrusSpringConfig.class);
                return null;
            }
        }).when(instanceProducer).set(any(CitrusConfiguration.class));

        InjectionHelper.inject(configurationProducer, "configurationInstance", instanceProducer);
        configurationProducer.configure(new BeforeSuite());


    }
}
