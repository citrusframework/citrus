/*
 * Copyright the original author or authors.
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

package org.citrusframework.container;

import org.citrusframework.CitrusContext;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.spi.BindToRegistry;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SequenceBeforeSuiteTest extends UnitTestSupport {

    @Test
    public void shouldBindAsBean() {
        CitrusContext citrusContext = CitrusContext.create();
        CitrusAnnotations.parseConfiguration(this, citrusContext);

        Assert.assertTrue(citrusContext.getReferenceResolver().isResolvable(SequenceBeforeSuite.class));
        SequenceBeforeSuite sequenceBeforeSuite = citrusContext.getReferenceResolver().resolve(SequenceBeforeSuite.class);

        Assert.assertEquals(sequenceBeforeSuite.getActions().size(), 1L);
        Assert.assertTrue(sequenceBeforeSuite.getActions().get(0) instanceof ReferenceResolverAwareTestAction);
        Assert.assertEquals(((ReferenceResolverAwareTestAction) sequenceBeforeSuite.getActions().get(0)).getReferenceResolver(), citrusContext.getReferenceResolver());
    }

    @BindToRegistry
    public SequenceBeforeSuite beforeSuite() {
        return SequenceBeforeSuite.Builder.beforeSuite()
                .actions(new ReferenceResolverAwareTestAction.Builder())
                .build();
    }

}
