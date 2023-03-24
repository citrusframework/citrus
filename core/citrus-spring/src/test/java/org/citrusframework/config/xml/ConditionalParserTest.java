/*
 * Copyright 2006-2011 the original author or authors.
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

package org.citrusframework.config.xml;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.citrusframework.container.Conditional;
import org.citrusframework.testng.AbstractActionParserTest;

/**
 * @author Matthias Beil
 * @since 1.2
 */
public class ConditionalParserTest extends AbstractActionParserTest<Conditional> {

    @Test
    public void testActionParser() {

        this.assertActionCount(1);
        this.assertActionClassAndName(Conditional.class, "conditional");

        final Conditional action = this.getNextTestActionFromTest();
        Assert.assertEquals(action.getActionCount(), 2);
    }

}
