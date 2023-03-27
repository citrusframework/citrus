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

package org.citrusframework.citrus.condition;

import org.citrusframework.citrus.UnitTestSupport;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Martin Maher
 * @since 2.4
 */
public class FileConditionTest extends UnitTestSupport {

    private final FileCondition condition = new FileCondition();

    @Test
    public void testValidFilename() {
        String filePath = "classpath:citrus.variables";
        condition.setFilePath(filePath);

        assertTrue(condition.isSatisfied(context));
    }

    @Test
    public void testValidFilenameWithVariables() {
        context.setVariable("file-name", "citrus.variables");
        String filePath = "classpath:${file-name}";
        condition.setFilePath(filePath);

        assertTrue(condition.isSatisfied(context));
    }

    @Test
    public void testInvalidFilename() {
        String filePath = "SomeMissingFile.xyz";
        condition.setFilePath(filePath);

        assertFalse(condition.isSatisfied(context));
    }
}
