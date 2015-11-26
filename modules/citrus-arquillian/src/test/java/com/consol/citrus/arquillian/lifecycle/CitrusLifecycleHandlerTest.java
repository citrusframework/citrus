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

package com.consol.citrus.arquillian.lifecycle;

import com.consol.citrus.Citrus;
import com.consol.citrus.arquillian.configuration.CitrusConfiguration;
import com.consol.citrus.arquillian.helper.InjectionHelper;
import com.consol.citrus.config.CitrusSpringConfig;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.core.api.Instance;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class CitrusLifecycleHandlerTest {

    private CitrusLifecycleHandler lifecycleHandler = new CitrusLifecycleHandler();

    private CitrusConfiguration configuration = CitrusConfiguration.from(new Properties());

    private Citrus citrusFramework = Citrus.newInstance(CitrusSpringConfig.class);
    private Instance<Citrus> citrusInstance = Mockito.mock(Instance.class);
    private Instance<CitrusConfiguration> configurationInstance = Mockito.mock(Instance.class);

    private DeployableContainer container = Mockito.mock(DeployableContainer.class);
    private DeploymentDescription deployment = Mockito.mock(DeploymentDescription.class);

    @Test
    public void testLifecycle() throws Exception {
        reset(citrusInstance, configurationInstance, container, deployment);

        when(citrusInstance.get()).thenReturn(citrusFramework);
        when(configurationInstance.get()).thenReturn(configuration);

        when(deployment.testable()).thenReturn(false);

        InjectionHelper.inject(lifecycleHandler, "citrusInstance", citrusInstance);
        InjectionHelper.inject(lifecycleHandler, "configurationInstance", configurationInstance);

        lifecycleHandler.beforeDeploy(new BeforeDeploy(container, deployment));
        lifecycleHandler.afterDeploy(new AfterUnDeploy(container, deployment));

    }
}