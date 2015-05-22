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
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

public class CitrusInstanceProducerTest {

    private CitrusInstanceProducer citrusInstanceProducer = new CitrusInstanceProducer();
    private InstanceProducer<Citrus> instanceProducer = EasyMock.createMock(InstanceProducer.class);

    @Test
    public void testCreateInstance() throws Exception {
        reset(instanceProducer);

        instanceProducer.set(anyObject(Citrus.class));
        expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override
            public Void answer() throws Throwable {
                Citrus citrus = (Citrus) getCurrentArguments()[0];
                Assert.assertNotNull(citrus);
                return null;
            }
        });

        replay(instanceProducer);

        InjectionHelper.inject(citrusInstanceProducer, "citrusInstance", instanceProducer);
        citrusInstanceProducer.beforeSuite(new BeforeSuite());

        verify(instanceProducer);
    }
}