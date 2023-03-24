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

package org.citrusframework.functions.core;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.apache.commons.codec.binary.Base64;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class ReadFileResourceFunctionTest extends UnitTestSupport {

    /**
     * Class under test
     */
    private final ReadFileResourceFunction function = new ReadFileResourceFunction();

    @Test
    public void testExecute() {
        context.setVariable("filename", "file.txt");
        context.setVariable("user", "Christoph");

        String path = "classpath:org/citrusframework/functions/${filename}";
        String result = function.execute(List.of(path), context);

        Assert.assertTrue(result.startsWith("This is a sample file content!"));
        Assert.assertTrue(result.contains("'Christoph'"));
    }

    @Test
    public void testExecuteBase64() {
        context.setVariable("filename", "file.txt");
        context.setVariable("user", "Christoph");

        String path = "classpath:org/citrusframework/functions/${filename}";
        String result = function.execute(List.of(path, "true", "true"), context);

        // Note that the file content should result in a constant and should not contain newline characters to run smoothly on linux and windows.
        String resolvedFileContent = "This is a sample file content! We can also use variables 'Christoph' and functions 1999-12-09";

        String expected = Base64.encodeBase64String(resolvedFileContent.getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(result, expected);
    }

    @Test
    public void testExecuteBase64NoReplace() {

        // By default variable replacement should not be performed on base64 encoding.
        String expectedBase64WithoutReplacement = "VGhpcyBpcyBhIHNhbXBsZSBmaWxlIGNvbnRlbnQhIFdlIGNhbiBhbHNvIHVzZSB2YXJpYWJsZXMgJyR7dXNlcn0nIGFuZCBmdW5jdGlvbnMgY2l0cnVzOmNoYW5nZURhdGUoJzIwMDAtMDEtMTAnLCAnLTFNLTFkJywgJ3l5eXktTU0tZGQnKQ==";

        context.setVariable("filename", "file.txt");
        context.setVariable("user", "Christoph");

        String path = "classpath:org/citrusframework/functions/${filename}";
        String result = function.execute(List.of(path, "true"), context);

        Assert.assertEquals(result, expectedBase64WithoutReplacement);
    }

    @Test(expectedExceptions = InvalidFunctionUsageException.class)
    public void testInvalidFunctionUsage() {
        function.execute(Collections.emptyList(), context);
    }

}
