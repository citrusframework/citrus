/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.condition;

import com.consol.citrus.context.TestContext;
import org.easymock.EasyMock;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Martin Maher
 * @since 2.4
 */
public class FileConditionTest {
    private TestContext contextMock = EasyMock.createMock(TestContext.class);

    @Test
    public void isSatisfiedShouldSucceedWithValidFilename() throws Exception {
        FileCondition testling = new FileCondition();
        String filePath = new ClassPathResource("citrus.variables").getFile().getAbsolutePath();
        testling.setFilePath(filePath);

        reset(contextMock);
        expect(contextMock.replaceDynamicContentInString(filePath)).andReturn(filePath).anyTimes();
        replay(contextMock);

        assertTrue(testling.isSatisfied(contextMock));

        verify(contextMock);
    }

    @Test
    public void isSatisfiedShouldFailDueToInvalidFilename() throws Exception {
        FileCondition testling = new FileCondition();
        String filePath = "SomeMissingFile.xyz";
        testling.setFilePath(filePath);

        reset(contextMock);
        expect(contextMock.replaceDynamicContentInString(filePath)).andReturn(filePath).anyTimes();
        replay(contextMock);

        assertFalse(testling.isSatisfied(contextMock));

        verify(contextMock);
    }
}