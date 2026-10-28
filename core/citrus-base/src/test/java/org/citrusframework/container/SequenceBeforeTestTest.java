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
import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.spi.BindToRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SequenceBeforeTestTest extends UnitTestSupport {

    @Test
    public void shouldBindAsBean() {
        CitrusContext citrusContext = CitrusContext.create();
        CitrusAnnotations.parseConfiguration(this, citrusContext);

        Assertions.assertTrue(citrusContext.getReferenceResolver().isResolvable(SequenceBeforeTest.class));
        SequenceBeforeTest sequenceBeforeTest = citrusContext.getReferenceResolver().resolve(SequenceBeforeTest.class);

        Assertions.assertEquals(1L, sequenceBeforeTest.getActions().size());
        Assertions.assertTrue(sequenceBeforeTest.getActions().get(0) instanceof ReferenceResolverAwareTestAction);
        Assertions.assertEquals(citrusContext.getReferenceResolver(), ((ReferenceResolverAwareTestAction) sequenceBeforeTest.getActions().get(0)).getReferenceResolver());
    }

    @BindToRegistry
    public SequenceBeforeTest beforeTest() {
        return SequenceBeforeTest.Builder.beforeTest()
                .actions(new ReferenceResolverAwareTestAction.Builder())
                .build();
    }
}
