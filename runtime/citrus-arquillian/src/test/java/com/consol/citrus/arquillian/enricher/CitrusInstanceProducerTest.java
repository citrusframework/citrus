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

import java.util.Properties;

import com.consol.citrus.Citrus;
import com.consol.citrus.arquillian.configuration.CitrusConfiguration;
import com.consol.citrus.arquillian.helper.InjectionHelper;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class CitrusInstanceProducerTest {

    private CitrusInstanceProducer citrusInstanceProducer = new CitrusInstanceProducer();

    private CitrusConfiguration configuration = CitrusConfiguration.from(new Properties());

    @Mock
    private InstanceProducer<Citrus> instanceProducer;
    @Mock
    private Instance<CitrusConfiguration> configurationInstance;
    @Mock
    private DeployableContainer<?> container;
    @Mock
    private DeploymentDescription deployment;

    @BeforeClass
    public void setupMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateInstance() throws Exception {
        reset(instanceProducer, configurationInstance, container, deployment);

        when(configurationInstance.get()).thenReturn(configuration);
        when(deployment.testable()).thenReturn(false);

        doAnswer(invocation -> {
            Citrus citrus = (Citrus) invocation.getArguments()[0];
            Assert.assertNotNull(citrus);
            return null;
        }).when(instanceProducer).set(any(Citrus.class));

        InjectionHelper.inject(citrusInstanceProducer, "citrusInstance", instanceProducer);
        InjectionHelper.inject(citrusInstanceProducer, "configurationInstance", configurationInstance);
        citrusInstanceProducer.beforeDeploy(new BeforeDeploy(container, deployment));

    }
}
