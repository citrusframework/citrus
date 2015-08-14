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
import com.consol.citrus.arquillian.helper.InjectionHelper;
import com.consol.citrus.dsl.design.DefaultTestDesigner;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.DefaultTestRunner;
import com.consol.citrus.dsl.runner.TestRunner;
import org.easymock.EasyMock;
import org.jboss.arquillian.core.api.Instance;
import org.springframework.util.ReflectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URL;

import static org.easymock.EasyMock.*;

public class CitrusTestEnricherTest {

    private CitrusTestEnricher testEnricher = new CitrusTestEnricher();

    private Citrus citrusFramework = Citrus.newInstance(ArquillianTestConfig.class);
    private Instance<Citrus> citrusInstance = EasyMock.createMock(Instance.class);

    @Test
    public void testEnrichTest() throws Exception {
        ArquillianTest testInstance = new ArquillianTest();

        reset(citrusInstance);
        expect(citrusInstance.get()).andReturn(citrusFramework).atLeastOnce();
        replay(citrusInstance);

        Assert.assertNull(testInstance.getCitrus());

        InjectionHelper.inject(testEnricher, "citrusInstance", citrusInstance);
        testEnricher.enrich(testInstance);

        Assert.assertNotNull(testInstance.getCitrus());
        Assert.assertNotNull(testInstance.getJmsEndpoint());
        Assert.assertEquals(testInstance.getJmsEndpoint().getName(), "jmsEndpoint");
        Assert.assertNotNull(testInstance.getSomeEndpoint());
        Assert.assertEquals(testInstance.getSomeEndpoint().getName(), "someEndpoint");
        Assert.assertNotNull(testInstance.getJmsSyncEndpoint());
        Assert.assertEquals(testInstance.getJmsSyncEndpoint().getName(), "jmsSyncEndpoint");

        verify(citrusInstance);
    }

    @Test
    public void testResolveTestMethod() throws Exception {
        reset(citrusInstance);
        expect(citrusInstance.get()).andReturn(citrusFramework).atLeastOnce();
        replay(citrusInstance);

        InjectionHelper.inject(testEnricher, "citrusInstance", citrusInstance);
        Object[] resolvedParameter = testEnricher.resolve(ReflectionUtils.findMethod(ArquillianTest.class, "testMethod", TestDesigner.class));
        Assert.assertEquals(resolvedParameter.length, 1L);
        Assert.assertEquals(resolvedParameter[0].getClass(), DefaultTestDesigner.class);

        resolvedParameter = testEnricher.resolve(ReflectionUtils.findMethod(ArquillianTest.class, "testMethod", URL.class, TestDesigner.class));
        Assert.assertEquals(resolvedParameter.length, 2L);
        Assert.assertNull(resolvedParameter[0]);
        Assert.assertEquals(resolvedParameter[1].getClass(), DefaultTestDesigner.class);

        resolvedParameter = testEnricher.resolve(ReflectionUtils.findMethod(ArquillianTest.class, "testMethod", TestDesigner.class, URL.class));
        Assert.assertEquals(resolvedParameter.length, 2L);
        Assert.assertEquals(resolvedParameter[0].getClass(), DefaultTestDesigner.class);
        Assert.assertNull(resolvedParameter[1]);

        resolvedParameter = testEnricher.resolve(ReflectionUtils.findMethod(ArquillianTest.class, "testMethod", TestRunner.class));
        Assert.assertEquals(resolvedParameter.length, 1L);
        Assert.assertEquals(resolvedParameter[0].getClass(), DefaultTestRunner.class);

        resolvedParameter = testEnricher.resolve(ReflectionUtils.findMethod(ArquillianTest.class, "testMethod", URL.class, TestRunner.class));
        Assert.assertEquals(resolvedParameter.length, 2L);
        Assert.assertNull(resolvedParameter[0]);
        Assert.assertEquals(resolvedParameter[1].getClass(), DefaultTestRunner.class);

        resolvedParameter = testEnricher.resolve(ReflectionUtils.findMethod(ArquillianTest.class, "testMethod", TestRunner.class, URL.class));
        Assert.assertEquals(resolvedParameter.length, 2L);
        Assert.assertEquals(resolvedParameter[0].getClass(), DefaultTestRunner.class);
        Assert.assertNull(resolvedParameter[1]);

        resolvedParameter = testEnricher.resolve(ReflectionUtils.findMethod(ArquillianTest.class, "testMethod", URL.class));
        Assert.assertEquals(resolvedParameter.length, 1L);
        Assert.assertNull(resolvedParameter[0]);

        resolvedParameter = testEnricher.resolve(ReflectionUtils.findMethod(ArquillianTest.class, "otherMethod"));
        Assert.assertEquals(resolvedParameter.length, 0L);

        verify(citrusInstance);
    }
}