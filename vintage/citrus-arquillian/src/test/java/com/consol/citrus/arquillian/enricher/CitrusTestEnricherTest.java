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

import java.net.URL;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusSpringContextProvider;
import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.arquillian.helper.InjectionHelper;
import org.jboss.arquillian.core.api.Instance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.ReflectionUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


public class CitrusTestEnricherTest {

    private CitrusTestEnricher testEnricher = new CitrusTestEnricher();

    private Citrus citrusFramework = Citrus.newInstance(new CitrusSpringContextProvider(ArquillianTestConfig.class));

    @Mock
    private Instance<Citrus> citrusInstance;

    @BeforeMethod
    public void setupMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testEnrichTest() throws Exception {
        ArquillianTest testInstance = new ArquillianTest();

        reset(citrusInstance);
        when(citrusInstance.get()).thenReturn(citrusFramework);

        Assert.assertNull(testInstance.getCitrus());

        InjectionHelper.inject(testEnricher, "citrusInstance", citrusInstance);
        testEnricher.enrich(testInstance);

        Assert.assertNotNull(testInstance.getCitrus());
        Assert.assertNotNull(testInstance.getDirectEndpoint());
        Assert.assertEquals(testInstance.getDirectEndpoint().getName(), "directEndpoint");
        Assert.assertNotNull(testInstance.getSomeEndpoint());
        Assert.assertEquals(testInstance.getSomeEndpoint().getName(), "someEndpoint");
        Assert.assertNotNull(testInstance.getDirectSyncEndpoint());
        Assert.assertEquals(testInstance.getDirectSyncEndpoint().getName(), "directSyncEndpoint");
    }

    @Test
    public void testResolveTestMethod() throws Exception {
        reset(citrusInstance);
        when(citrusInstance.get()).thenReturn(citrusFramework);

        InjectionHelper.inject(testEnricher, "citrusInstance", citrusInstance);
        Object[] resolvedParameter = testEnricher.resolve(ReflectionUtils.findMethod(ArquillianTest.class, "testMethod", TestCaseRunner.class));
        Assert.assertEquals(resolvedParameter.length, 1L);
        Assert.assertEquals(resolvedParameter[0].getClass(), DefaultTestCaseRunner.class);

        resolvedParameter = testEnricher.resolve(ReflectionUtils.findMethod(ArquillianTest.class, "testMethod", URL.class, TestCaseRunner.class));
        Assert.assertEquals(resolvedParameter.length, 2L);
        Assert.assertNull(resolvedParameter[0]);
        Assert.assertEquals(resolvedParameter[1].getClass(), DefaultTestCaseRunner.class);

        resolvedParameter = testEnricher.resolve(ReflectionUtils.findMethod(ArquillianTest.class, "testMethod", TestCaseRunner.class, URL.class));
        Assert.assertEquals(resolvedParameter.length, 2L);
        Assert.assertEquals(resolvedParameter[0].getClass(), DefaultTestCaseRunner.class);
        Assert.assertNull(resolvedParameter[1]);

        resolvedParameter = testEnricher.resolve(ReflectionUtils.findMethod(ArquillianTest.class, "testMethod", URL.class));
        Assert.assertEquals(resolvedParameter.length, 1L);
        Assert.assertNull(resolvedParameter[0]);

        resolvedParameter = testEnricher.resolve(ReflectionUtils.findMethod(ArquillianTest.class, "otherMethod"));
        Assert.assertEquals(resolvedParameter.length, 0L);
    }
}
