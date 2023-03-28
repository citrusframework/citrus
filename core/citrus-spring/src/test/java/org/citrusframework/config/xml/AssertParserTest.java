/*
 * Copyright 2006-2010 the original author or authors.
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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testng.AbstractActionParserTest;

/**
 * @author Christoph Deppisch
 */
public class AssertParserTest extends AbstractActionParserTest<org.citrusframework.container.Assert> {

    @Test
    public void testAssertParser() {
        assertActionCount(1);
        assertActionClassAndName(org.citrusframework.container.Assert.class, "assert");
        
        org.citrusframework.container.Assert action = getNextTestActionFromTest();
        Assert.assertEquals(action.getDescription(), "This action asserts an exception in nested actions");
        Assert.assertEquals(action.getMessage(), "This went wrong");
        Assert.assertEquals(action.getException(), CitrusRuntimeException.class);
    }
}
