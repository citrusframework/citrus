/*
 * Copyright 2006-2012 the original author or authors.
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

package org.citrusframework.citrus.dsl.design;

import java.util.HashMap;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.TestCaseMetaInfo.Status;
import org.citrusframework.citrus.container.SequenceAfterTest;
import org.citrusframework.citrus.container.SequenceBeforeTest;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.report.TestActionListeners;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Christoph Deppisch
 */
public class DefaultTestDesignerTest {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    @Test
    public void testCitrusTestDesigner() {
        reset(referenceResolver);

        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                description("This is a Test");
                author("Christoph");
                status(Status.FINAL);

                echo("Hello Citrus!");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getName(), "");
        Assert.assertEquals(test.getPackageName(), "org.citrusframework.citrus.dsl.design");

        Assert.assertEquals(test.getDescription(), "This is a Test");

        Assert.assertEquals(test.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(test.getMetaInfo().getStatus(), Status.FINAL);

    }

}
