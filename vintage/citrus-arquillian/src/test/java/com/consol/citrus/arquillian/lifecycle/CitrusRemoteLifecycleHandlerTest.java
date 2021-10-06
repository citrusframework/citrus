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

import java.util.Properties;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusSpringContextProvider;
import com.consol.citrus.arquillian.configuration.CitrusConfiguration;
import com.consol.citrus.arquillian.helper.InjectionHelper;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;


public class CitrusRemoteLifecycleHandlerTest {

    private CitrusRemoteLifecycleHandler lifecycleHandler = new CitrusRemoteLifecycleHandler();

    private CitrusConfiguration configuration = CitrusConfiguration.from(new Properties());

    private Citrus citrusFramework = Citrus.newInstance(new CitrusSpringContextProvider());

    @Mock
    private Instance<Citrus> citrusInstance;
    @Mock
    private Instance<CitrusConfiguration> configurationInstance;

    @BeforeClass
    public void setupMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLifecycle() throws Exception {
        when(citrusInstance.get()).thenReturn(citrusFramework);
        when(configurationInstance.get()).thenReturn(configuration);

        InjectionHelper.inject(lifecycleHandler, "citrusInstance", citrusInstance);
        InjectionHelper.inject(lifecycleHandler, "configurationInstance", configurationInstance);

        lifecycleHandler.beforeSuite(new BeforeSuite());
        lifecycleHandler.afterSuite(new AfterSuite());

    }
}
