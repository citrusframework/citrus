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

import com.consol.citrus.arquillian.configuration.CitrusConfiguration;
import com.consol.citrus.arquillian.helper.InjectionHelper;
import com.consol.citrus.config.CitrusBaseConfig;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.easymock.EasyMock.verify;

public class CitrusRemoteConfigurationProducerTest {

    private CitrusRemoteConfigurationProducer configurationProducer = new CitrusRemoteConfigurationProducer();
    private InstanceProducer<CitrusConfiguration> instanceProducer = EasyMock.createMock(InstanceProducer.class);

    @Test
    public void testConfigure() throws Exception {
        reset(instanceProducer);

        instanceProducer.set(anyObject(CitrusConfiguration.class));
        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                CitrusConfiguration configuration = (CitrusConfiguration) getCurrentArguments()[0];
                Assert.assertEquals(configuration.getCitrusVersion(), "latest");
                Assert.assertTrue(configuration.isAutoPackage());
                Assert.assertEquals(configuration.getSuiteName(), "remoteSuite");
                Assert.assertEquals(configuration.getConfigurationClass(), CitrusBaseConfig.class);

                return null;
            }
        });

        replay(instanceProducer);

        InjectionHelper.inject(configurationProducer, "configurationInstance", instanceProducer);
        configurationProducer.configure(new BeforeSuite());

        verify(instanceProducer);

    }
}