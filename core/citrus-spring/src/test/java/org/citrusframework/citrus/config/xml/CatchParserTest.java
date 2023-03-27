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

package org.citrusframework.citrus.config.xml;

import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.citrusframework.citrus.actions.FailAction;
import org.citrusframework.citrus.container.Catch;
import org.citrusframework.citrus.testng.AbstractActionParserTest;

/**
 * @author Christoph Deppisch
 */
public class CatchParserTest extends AbstractActionParserTest<Catch> {

    @Test
    public void testCatchParser() {
        assertActionCount(1);
        assertActionClassAndName(Catch.class, "catch");
        
        Catch action = getNextTestActionFromTest();
        Assert.assertEquals(action.getException(), CitrusRuntimeException.class.getName());
        Assert.assertEquals(action.getActionCount(), 1);
        Assert.assertEquals(action.getActions().get(0).getClass(), FailAction.class);
    }
}
